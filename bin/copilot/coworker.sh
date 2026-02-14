#!/usr/bin/env bash

# Find the first parent directory that contains a VERSION file
AppHome="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
while [[ ! -f "$AppHome/VERSION" ]] && [[ "$AppHome" != "/" ]]; do
    AppHome="$(dirname "$AppHome")"
done

cd "$AppHome"

baseDir="$AppHome/docs-dev/copilot/tasks/daily"
createdDir="$baseDir/created"
workingDir="$baseDir/working"
finishedDir="$baseDir/finished"
repoRoot="$AppHome"

# Ensure directories exist
mkdir -p "$createdDir"
mkdir -p "$workingDir"
mkdir -p "$finishedDir"

# Process each file in the created directory
for file in "$createdDir"/*; do
    # Skip if directory is empty
    [[ -e "$file" ]] || continue

    echo "Processing $(basename "$file")..."

    # Read file content
    content=$(cat "$file")

    title=""
    description=""
    prompt=""

    # Try to parse structured content
    # Format expected:
    # Title: ...
    # Description: ...
    # Prompt: ...
    if [[ $content =~ ^Title:[[:space:]]*([^$'\n']+)$'\n'Description:[[:space:]]*([^$'\n']+)$'\n'Prompt:[[:space:]]*(.*)$ ]]; then
        title="${BASH_REMATCH[1]}"
        description="${BASH_REMATCH[2]}"
        prompt="${BASH_REMATCH[3]}"
    else
        # Fallback: Treat entire content as prompt
        title="$(basename "$file" | sed 's/\.[^.]*$//')"
        description="Task from $(basename "$file")"
        prompt="$content"
    fi

    # Sanitize title for filename
    safeTitle=$(echo "$title" | sed 's/[\/\\*?:"<>|]/_/g')
    fileExt="${file##*.}"
    if [[ "$file" == *.* ]]; then
        newFileName="${safeTitle}.${fileExt}"
    else
        newFileName="$safeTitle"
    fi

    # Define paths
    workingPath="$workingDir/$newFileName"
    finishedPath="$finishedDir/$newFileName"
    logPath="$finishedDir/${newFileName}.log"

    # Move to working directory (renaming if needed)
    mv "$file" "$workingPath"
    echo "Moved to working: $workingPath"

    # Change to repo root for execution
    pushd "$repoRoot" > /dev/null || exit 1

    echo "Executing Copilot for task: $title"
    echo "Prompt: $prompt"

    {
        # Prepare log files
        stdOutLog="${logPath}.stdout"
        stdErrLog="${logPath}.stderr"

        # Execute copilot and capture output
        if copilot -p "$prompt" --allow-all-tools --allow-all-paths > "$stdOutLog" 2> "$stdErrLog"; then
            exitCode=$?
        else
            exitCode=$?
        fi

        # Combine logs
        if [[ -f "$stdOutLog" ]]; then
            cat "$stdOutLog" >> "$logPath"
        fi
        if [[ -f "$stdErrLog" && -s "$stdErrLog" ]]; then
            {
                echo ""
                echo "=== STDERR ==="
                echo ""
                cat "$stdErrLog"
            } >> "$logPath"
        fi

        # Cleanup temp logs
        rm -f "$stdOutLog"
        rm -f "$stdErrLog"

        echo "Copilot execution finished with exit code $exitCode"

        if [[ $exitCode -ne 0 ]]; then
            echo "Warning: Copilot exited with non-zero code. Check log: $logPath" >&2
        fi
    } || {
        echo "Failed to execute copilot: $?" >&2
        {
            echo ""
            echo "Error executing copilot"
        } >> "$logPath"
    }

    # Return to previous directory
    popd > /dev/null || exit 1

    # Move to finished
    mv "$workingPath" "$finishedPath"
    echo "Task moved to finished: $finishedPath"
    echo "---------------------------------------------------"
done

