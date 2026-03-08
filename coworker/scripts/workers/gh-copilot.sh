#!/usr/bin/env bash

get_gh_copilot_repo_root() {
    local script_dir
    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    if git_root=$(git rev-parse --show-toplevel 2>/dev/null); then
        printf '%s\n' "$git_root"
        return 0
    fi

    local current_dir="$script_dir"
    while [[ -n "$current_dir" && "$current_dir" != "/" ]]; do
        if [[ -f "$current_dir/ROOT.md" ]]; then
            printf '%s\n' "$current_dir"
            return 0
        fi
        current_dir="$(dirname "$current_dir")"
    done

    echo "Repo root not found." >&2
    return 1
}

load_gh_copilot_command() {
    local repo_root="${1:-$(get_gh_copilot_repo_root)}"
    local config_sh="$repo_root/coworker/scripts/config.sh"

    if [[ -f "$config_sh" ]]; then
        # shellcheck disable=SC1090
        source "$config_sh"
    fi

    if ! declare -p COPILOT >/dev/null 2>&1; then
        COPILOT=(gh copilot)
    fi

    if [[ "$(declare -p COPILOT 2>/dev/null)" != declare\ -a* ]]; then
        echo "Error: COPILOT must be defined as a bash array in $config_sh" >&2
        return 1
    fi

    if [[ ${#COPILOT[@]} -lt 2 ]]; then
        echo "Error: COPILOT must include an executable and at least one argument" >&2
        return 1
    fi

    GHCOPILOT_REPO_ROOT="$repo_root"
    GHCOPILOT_EXECUTABLE="${COPILOT[0]}"
    GHCOPILOT_BASE_ARGS=("${COPILOT[@]:1}")
}

new_gh_copilot_args() {
    local prompt="$1"
    shift || true

    GHCOPILOT_LAST_ARGS=("${GHCOPILOT_BASE_ARGS[@]}")
    GHCOPILOT_LAST_ARGS+=(-- -p "$prompt")

    if [[ $# -gt 0 ]]; then
        GHCOPILOT_LAST_ARGS+=("$@")
    fi
}

format_gh_copilot_command() {
    local formatted="$GHCOPILOT_EXECUTABLE"
    local arg quoted

    for arg in "${GHCOPILOT_LAST_ARGS[@]}"; do
        printf -v quoted '%q' "$arg"
        formatted+=" $quoted"
    done

    printf '%s\n' "$formatted"
}

invoke_gh_copilot() {
    local prompt="$1"
    shift || true

    new_gh_copilot_args "$prompt" "$@"
    "$GHCOPILOT_EXECUTABLE" "${GHCOPILOT_LAST_ARGS[@]}"
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
    prompt="${1:-}"
    shift || true

    if [[ -z "$prompt" ]]; then
        echo "Usage: $0 <prompt> [additional gh copilot args...]" >&2
        exit 1
    fi

    load_gh_copilot_command
    invoke_gh_copilot "$prompt" "$@"
fi
