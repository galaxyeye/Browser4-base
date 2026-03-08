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
TARGET="$REPO_ROOT/coworker/scripts/deprecated/task-source-monitor.sh"

echo "WARNING: task-source-monitor.sh is deprecated as a scheduler entry point. Use coworker/scripts/coworker-scheduler.ps1 for recurring runs." >&2
if [[ -x "$TARGET" ]]; then
    exec "$TARGET" "$@"
fi

exec bash "$TARGET" "$@"

