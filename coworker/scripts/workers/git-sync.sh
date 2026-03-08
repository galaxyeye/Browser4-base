#!/usr/bin/env bash

# 🔍 Find the repo root using git
repoRoot=$(git rev-parse --show-toplevel 2>/dev/null)
if [[ -z "$repoRoot" ]]; then
    echo "Repo root not found. Exiting."
    exit 1
fi
cd "$repoRoot"

GH_COPILOT_HELPER="$repoRoot/coworker/scripts/workers/gh-copilot.sh"
# shellcheck disable=SC1090
source "$GH_COPILOT_HELPER"
load_gh_copilot_command "$repoRoot"

# Call copilot to commit all changes with a message
prompt="Commit all changes in $repoRoot and push to the remote repository. Resolve conflicts if there is any."
new_gh_copilot_args "$prompt" --allow-all-tools
echo "Running: $(format_gh_copilot_command)"
"$GHCOPILOT_EXECUTABLE" "${GHCOPILOT_LAST_ARGS[@]}"
