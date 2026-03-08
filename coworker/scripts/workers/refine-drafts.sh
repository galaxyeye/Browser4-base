#!/usr/bin/env bash
set -euo pipefail

INPUT_PATH="${1:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

find_repo_root() {
    local current_dir="$SCRIPT_DIR"

    while [[ -n "$current_dir" && "$current_dir" != "/" ]]; do
        if [[ -f "$current_dir/ROOT.md" || -d "$current_dir/.git" ]]; then
            printf '%s\n' "$current_dir"
            return 0
        fi
        current_dir="$(dirname "$current_dir")"
    done

    echo "Repo root not found." >&2
    return 1
}

GH_COPILOT_HELPER="$SCRIPT_DIR/gh-copilot.sh"
# shellcheck disable=SC1090
source "$GH_COPILOT_HELPER"

REPO_ROOT="$(find_repo_root)"
load_gh_copilot_command "$REPO_ROOT"

REFINE_ROOT="$REPO_ROOT/coworker/tasks/0draft/refine"
READY_DIR="$REFINE_ROOT/1ready"
WORKING_DIR="$REFINE_ROOT/2working"
DONE_DIR="$REFINE_ROOT/3done"

mkdir -p "$READY_DIR" "$WORKING_DIR" "$DONE_DIR"

if [[ -z "$INPUT_PATH" ]]; then
    INPUT_PATH="$READY_DIR"
fi

resolve_unique_path() {
    local directory="$1"
    local base_name="$2"
    local extension="$3"

    local candidate="$directory/${base_name}${extension}"
    if [[ ! -e "$candidate" ]]; then
        printf '%s\n' "$candidate"
        return
    fi

    local counter=2
    while true; do
        local next_candidate="$directory/${base_name}.${counter}${extension}"
        if [[ ! -e "$next_candidate" ]]; then
            printf '%s\n' "$next_candidate"
            return
        fi
        ((counter++))
    done
}

get_refine_targets() {
    local input_path="$1"

    if [[ ! -e "$input_path" ]]; then
        echo "Refine path not found: $input_path" >&2
        return 1
    fi

    if [[ -d "$input_path" ]]; then
        find "$input_path" -maxdepth 1 -type f | sort
        return
    fi

    printf '%s\n' "$input_path"
}

invoke_draft_refinement() {
    local working_file="$1"
    local draft_content
    draft_content="$(cat "$working_file")"

    local prompt
    prompt=$(cat <<EOF
Refine the following draft for clarity, coherence, and relevance to the intended audience.
Preserve the original intent and useful structure unless a change materially improves the draft.
Return only the complete refined document content with no surrounding code fences or commentary.

Source file: $working_file

--- BEGIN DRAFT ---
$draft_content
--- END DRAFT ---
EOF
)

    invoke_gh_copilot "$prompt" --allow-all-tools --allow-all-paths
}

TARGETS=()
while IFS= read -r target; do
    TARGETS+=("$target")
done < <(get_refine_targets "$INPUT_PATH")

if [[ ${#TARGETS[@]} -eq 0 ]]; then
    echo "No draft files found in $INPUT_PATH"
    exit 0
fi

failure_count=0

for target in "${TARGETS[@]}"; do
    file_name="$(basename "$target")"
    if [[ "$file_name" == *.* ]]; then
        extension=".${file_name##*.}"
        base_name="${file_name%.*}"
    else
        extension=""
        base_name="$file_name"
    fi

    working_path="$(resolve_unique_path "$WORKING_DIR" "$base_name" "$extension")"
    mv "$target" "$working_path"
    echo "Moved draft to working: $working_path"

    if refined_content="$(invoke_draft_refinement "$working_path")"; then
        refined_content="${refined_content%$'\n'}"

        if [[ -z "${refined_content//[$'\t\r\n ']}" ]]; then
            echo "Failed to refine $(basename "$working_path"): GitHub Copilot returned empty output" >&2
            failure_count=$((failure_count + 1))
            continue
        fi

        printf '%s\n' "$refined_content" > "$working_path"
        done_path="$(resolve_unique_path "$DONE_DIR" "$base_name" "$extension")"
        mv "$working_path" "$done_path"
        echo "Refined draft moved to done: $done_path"
    else
        echo "Failed to refine $(basename "$working_path")" >&2
        failure_count=$((failure_count + 1))
    fi
done

if [[ $failure_count -gt 0 ]]; then
    exit 1
fi
