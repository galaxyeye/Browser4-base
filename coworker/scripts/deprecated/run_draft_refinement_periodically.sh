#!/usr/bin/env bash
set -euo pipefail

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

    echo "Repo root not found. Exiting." >&2
    return 1
}

REPO_ROOT="$(find_repo_root)"

SCAN_PATH="$REPO_ROOT/coworker/tasks/0draft/refine/1ready"
INTERVAL_SECONDS=15
ONCE=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --path)
            SCAN_PATH="$2"
            shift 2
            ;;
        --interval)
            INTERVAL_SECONDS="$2"
            shift 2
            ;;
        --once)
            ONCE=true
            shift
            ;;
        *)
            echo "Unknown argument: $1" >&2
            exit 1
            ;;
    esac
done

REFINE_SCRIPT="$REPO_ROOT/coworker/scripts/workers/refine-drafts.sh"

echo "Monitoring draft refinement path: $SCAN_PATH"
echo "Refine script: $REFINE_SCRIPT"

while true; do
    pending_count=0

    if [[ -e "$SCAN_PATH" ]]; then
        if [[ -d "$SCAN_PATH" ]]; then
            pending_count=$(find "$SCAN_PATH" -maxdepth 1 -type f | wc -l | tr -d ' ')
        else
            pending_count=1
        fi
    fi

    timestamp=$(date -u '+%Y-%m-%d %H:%M:%S')
    if [[ "$pending_count" -eq 0 ]]; then
        echo "$timestamp - No draft files found for refinement."
        if [[ "$ONCE" == true ]]; then
            exit 0
        fi

        sleep "$INTERVAL_SECONDS"
        continue
    fi

    echo "$timestamp - Refining $pending_count draft file(s)..."
    if [[ -x "$REFINE_SCRIPT" ]]; then
        if "$REFINE_SCRIPT" "$SCAN_PATH"; then
            exit_code=0
        else
            exit_code=$?
        fi
    else
        if bash "$REFINE_SCRIPT" "$SCAN_PATH"; then
            exit_code=0
        else
            exit_code=$?
        fi
    fi

    if [[ "$exit_code" -ne 0 ]]; then
        echo "$timestamp - Draft refinement finished with exit code $exit_code." >&2
    fi

    if [[ "$ONCE" == true ]]; then
        exit "$exit_code"
    fi

    sleep "$INTERVAL_SECONDS"
done
