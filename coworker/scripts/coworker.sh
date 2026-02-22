#!/usr/bin/env bash

# ============================================================================
# Coworker Task Runner - Bash Shell Version
# ============================================================================
# Purpose:
#   Automatically processes task files in the 'created' directory
#   and executes them using the Copilot tool. Task files are moved through
#   a workflow: created -> working -> finished, with execution logs recorded.
#
# Task File Format (optional structured format):
#   Title: <task title>
#   Description: <task description>
#   Prompt: <task prompt content>
#
#   If not in structured format, the entire file content is treated as the prompt.
#
# Usage:
#   bash coworker.sh
#   ./coworker.sh
# ============================================================================


# Handle optional TaskFile argument
taskFile="$1"

# This allows the script to be run from any location within the project
AppHome="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repoRoot="$AppHome"
while [[ ! -f "$repoRoot/ROOT.md" ]] && [[ "$repoRoot" != "/" ]]; do
    repoRoot="$(dirname "$repoRoot")"
done

cd "$repoRoot"

# Define directory paths for task management workflow
baseDir="$repoRoot/coworker/tasks"
createdDir="$baseDir/1created"        # Input directory for new tasks
workingDir="$baseDir/2working"        # Processing directory for current tasks
finishedDir="$baseDir/3finished"      # Output directory for completed tasks
logsDir="$baseDir/logs"              # Directory for script and execution logs
repoRoot="$repoRoot"                  # Repository root for Copilot execution

# Ensure all required directories exist
# Create them if they don't already exist
mkdir -p "$createdDir"
mkdir -p "$workingDir"
mkdir -p "$finishedDir"
mkdir -p "$logsDir"

# Handle specified TaskFile
if [[ -n "$taskFile" ]]; then
    if [[ -f "$taskFile" ]]; then
        fileName=$(basename "$taskFile")
        destPath="$createdDir/$fileName"
        mv "$taskFile" "$destPath"
        echo "Moved specified task file to: $destPath"
    else
        echo "Error: Specified task file not found: $taskFile" >&2
        exit 1
    fi
fi

# Initialize script-level logging
# Main log file for all script output
scriptLogPath="$logsDir/coworker-$(date +%Y%m%d-%H%M%S).log"
scriptStartTime=$(date '+%Y-%m-%d %H:%M:%S')

# ============================================================================
# Logging Functions
# ============================================================================

# Function: Write message to console and main script log file
# Usage: log_message "message text" [LEVEL]
# Levels: INFO (default), WARN, ERROR
log_message() {
    local message="$1"
    local level="${2:-INFO}"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    local logEntry="[$timestamp] [$level] $message"

    # Write to console
    case "$level" in
        WARN)
            echo -e "\033[33m$logEntry\033[0m"  # Yellow for warnings
            ;;
        ERROR)
            echo -e "\033[31m$logEntry\033[0m" >&2  # Red for errors
            ;;
        *)
            echo "$logEntry"
            ;;
    esac

    # Append to script log file
    echo "$logEntry" >> "$scriptLogPath"
}

# Function: Write message only to log file (for verbose output)
# Usage: log_verbose "debug message"
log_verbose() {
    local message="$1"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    local logEntry="[$timestamp] [DEBUG] $message"

    # Append to script log file only (not console)
    echo "$logEntry" >> "$scriptLogPath"
}

# Log script startup
log_message "===========================================================================" INFO
log_message "Coworker Task Runner - Bash Shell Version" INFO
log_message "Started at: $scriptStartTime" INFO
log_message "Script Log: $scriptLogPath" INFO
log_message "==========================================================================" INFO

# Process each file in the created directory
for file in "$createdDir"/*; do
    # Skip if directory is empty
    [[ -e "$file" ]] || continue

    log_message "Processing $(basename "$file")..." INFO

    # 1. Determine the descriptive name based on content
    scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    renameScript="$scriptDir/rename.sh"
    descriptiveName=""

    # Read content for basic info
    content=$(cat "$file")
    title="$(basename "$file" | sed 's/\.[^.]*$//')"
    safeTitle=$(echo "$title" | sed 's/[\/\\*?:"<>|]/_/g')

    chmod +x "$renameScript" 2>/dev/null

        if [[ -f "$renameScript" && -x "$renameScript" ]]; then
            # Execute rename.sh on the file in created dir
            generatedName=$("$renameScript" "$file")
            # Check for valid output (not empty, no spaces if possible, not Error)
            if [[ -n "$generatedName" && "$generatedName" != "Error"* ]]; then
                 # Basic validation - if it contains spaces, replace with dashes
                 generatedName=$(echo "$generatedName" | tr ' ' '-')
                 descriptiveName="$generatedName"
            fi
        else
            # Fallback to internal renaming logic if rename.sh is missing
            # Extract first 600 chars for prompt context
            promptSample=$(head -c 600 "$file")
            
            # Escape quotes for JSON/shell safety
            promptEscaped=$(echo "$promptSample" | sed 's/"/\\"/g')
            
            namingPrompt="Create a short, descriptive task name in kebab-case (3-6 words max). Output only the name.
Title: $safeTitle
Description: Task from $(basename "$file")
Prompt: $promptEscaped"

            # Call gh copilot directly using the same arguments as the PowerShell script
            # Assuming 'gh copilot' supports -p based on existing PS1 logic
            if command -v gh &> /dev/null; then
                 generatedName=$(gh copilot -p "$namingPrompt" --allow-all-tools --allow-all-paths 2>/dev/null | head -n 1)
            fi
            
            # Clean up the name (remove spaces, special chars, ensure kebab-case)
            if [[ -n "$generatedName" ]]; then
                generatedName=$(echo "$generatedName" | tr -d '[:space:]' | sed 's/[^a-zA-Z0-9._-]/-/g' | sed 's/--*/-/g' | sed 's/^-//' | sed 's/-$//' | head -c 60)
                if [[ -n "$generatedName" ]]; then
                    descriptiveName="$generatedName"
                fi
            fi
        fi

    if [[ -z "$descriptiveName" ]]; then
         descriptiveName="$safeTitle"
    fi
    
    # 2. Rename in place (in created dir)
    # Only rename if the name is different
    fileName=$(basename "$file")
    
    if [[ "$fileName" == *.* ]]; then
      fileExt="${fileName##*.}"
      baseName="${fileName%.*}"
      dotExt=".$fileExt"
    else
      fileExt=""
      baseName="$fileName"
      dotExt=""
    fi
    
    if [[ "$descriptiveName" != "$baseName" ]]; then
        newCreatedName="${descriptiveName}${dotExt}"
        renamedPath="$createdDir/$newCreatedName"
        
        # Collision handling in created dir
        if [[ -e "$renamedPath" ]]; then
            counter=2
            while [[ -e "$createdDir/$descriptiveName.$counter$dotExt" ]]; do
                ((counter++))
            done
            newCreatedName="$descriptiveName.$counter$dotExt"
            renamedPath="$createdDir/$newCreatedName"
            # Update descriptiveName to reflect the conflict resolution
            descriptiveName="$descriptiveName.$counter"
        fi
        
        mv "$file" "$renamedPath"
        log_message "Renamed in created: $fileName -> $newCreatedName" INFO
        
        # Update file variable to point to new path
        file="$renamedPath"
        # Update content variable re-read is not needed as content didn't change, but variable names did
        # We need to ensure subsequent logic uses the correct file path
    fi
    
    # 3. Move to working directory
    # Re-calculate basename/ext from the (possibly renamed) file
    currentFileName=$(basename "$file")
    
    if [[ "$currentFileName" == *.* ]]; then
      currentExt="${currentFileName##*.}"
      currentBaseName="${currentFileName%.*}"
      currentDotExt=".$currentExt"
    else
      currentExt=""
      currentBaseName="$currentFileName"
      currentDotExt=""
    fi

    workingPath="$workingDir/$currentFileName"

    # Handle filename collision in working dir
    if [[ -e "$workingPath" ]]; then
        counter=2
        while [[ -e "$workingDir/$currentBaseName.$counter$currentDotExt" ]]; do
            ((counter++))
        done
        newWorkingName="$currentBaseName.$counter$currentDotExt"
        workingPath="$workingDir/$newWorkingName"
    fi
    
    mv "$file" "$workingPath"
    log_message "Moved to working: $workingPath" INFO
    
    # Initialize variables for execution
    description="Task from $(basename "$file")"
    prompt="$content"

    # Try to parse structured content
    if [[ $content =~ ^Title:[[:space:]]*([^$'\n']+)$'\n'Description:[[:space:]]*([^$'\n']+)$'\n'Prompt:[[:space:]]*(.*)$ ]]; then
        title="${BASH_REMATCH[1]}"
        description="${BASH_REMATCH[2]}"
        prompt="${BASH_REMATCH[3]}"
    fi

    # Task log path
    taskLogPath="$logsDir/task_${newFileName}_$(date +%Y%m%d-%H%M%S).log"
    copilotLogPath="$logsDir/copilot_${newFileName}_$(date +%Y%m%d-%H%M%S).log"

    log_verbose "Task log will be written to: $taskLogPath"

    # Change to repository root directory for execution
    pushd "$repoRoot" > /dev/null || exit 1

    log_message "Executing Copilot for task: $descriptiveName" INFO
    log_verbose "Prompt length: ${#prompt} characters"

    # Record task execution details to task log
    {
        echo "Task: $descriptiveName"
        echo "Description: $description"
        echo "Original File: $(basename "$file")"
        echo "Started: $(date '+%Y-%m-%d %H:%M:%S')"
        echo "Prompt:"
        echo "$prompt"
        echo "---"
        echo "Copilot Execution Output:"
    } > "$taskLogPath"

    # Execute Copilot and handle logging and error handling
    {
        # Define paths for temporary output and error logs
        stdOutLog="${copilotLogPath}.stdout"
        stdErrLog="${copilotLogPath}.stderr"

        # Execute copilot tool with the task prompt
        # Capture both standard output and standard error to separate files
        if gh copilot -p "$prompt" --allow-all-tools --allow-all-paths > "$stdOutLog" 2> "$stdErrLog"; then
            exitCode=$?
        else
            exitCode=$?
        fi

        # Combine copilot stdout and stderr logs into the copilot-specific log
        # First append stdout if it exists
        if [[ -f "$stdOutLog" ]]; then
            cat "$stdOutLog" >> "$copilotLogPath"
        fi
        # Then append stderr if it exists and contains content
        if [[ -f "$stdErrLog" && -s "$stdErrLog" ]]; then
            {
                echo ""
                echo "=== COPILOT STDERR ==="
                echo ""
                cat "$stdErrLog"
            } >> "$copilotLogPath"
        fi

        # Clean up temporary log files
        rm -f "$stdOutLog"
        rm -f "$stdErrLog"

        log_message "Copilot execution finished with exit code $exitCode" INFO
        log_verbose "Copilot external tool log: $copilotLogPath"

        # Append copilot result to task log
        {
            echo ""
            echo "Copilot Exit Code: $exitCode"
            echo "Copilot Log: $copilotLogPath"
        } >> "$taskLogPath"

        # Warn if Copilot exited with an error code
        if [[ $exitCode -ne 0 ]]; then
            log_message "Warning: Copilot exited with non-zero code. Check log: $copilotLogPath" WARN
        fi
    } || {
        # Handle any errors that occur during script execution
        log_message "Failed to execute copilot: $?" ERROR
        {
            echo ""
            echo "Error executing copilot"
        } >> "$taskLogPath"
    }

    # Return to previous directory from pushd
    popd > /dev/null || exit 1

    # Move completed task from working directory to finished directory
    # Create date-based subdirectory: YYYY/MMDD
    currentYear=$(date +%Y)
    currentDate=$(date +%m%d)
    finishedSubDir="$finishedDir/$currentYear/$currentDate"
    mkdir -p "$finishedSubDir"

    finishedPath="$finishedSubDir/$newFileName"

    mv "$workingPath" "$finishedPath"
    log_message "Task moved to finished: $finishedPath" INFO
    log_message "---" INFO
done

# Log script completion
scriptEndTime=$(date '+%Y-%m-%d %H:%M:%S')
log_message "===========================================================================" INFO
log_message "All tasks completed" INFO
log_message "Ended at: $scriptEndTime" INFO
log_message "Script Log: $scriptLogPath" INFO
log_message "==========================================================================" INFO

