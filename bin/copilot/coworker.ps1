#!/usr/bin/env pwsh

# ============================================================================
# Coworker Task Runner - PowerShell Version
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
#   powershell -ExecutionPolicy Bypass -File coworker.ps1
# ============================================================================

# Find the first parent directory that contains a VERSION file
# This allows the script to be run from any location within the project
$AppHome = (Get-Item -Path $MyInvocation.MyCommand.Path).Directory
while ($AppHome -ne $null -and -not (Test-Path (Join-Path $AppHome "VERSION"))) {
    $AppHome = Split-Path -Parent $AppHome
}
Set-Location $AppHome

# Define directory paths for task management workflow
$baseDir = Join-Path $AppHome "docs-dev\copilot\tasks\daily"
$createdDir = Join-Path $baseDir "created"        # Input directory for new tasks
$workingDir = Join-Path $baseDir "working"        # Processing directory for current tasks
$finishedDir = Join-Path $baseDir "finished"      # Output directory for completed tasks
$logsDir = Join-Path $baseDir "logs"              # Directory for script and execution logs
$repoRoot = $AppHome                              # Repository root for Copilot execution

$extraTasksRoot = Join-Path $AppHome "docs-dev\tasks"
$taskRoots = @(
    @{
        Created = $createdDir
        Working = $workingDir
        Finished = $finishedDir
        Logs = $logsDir
        Label = "copilot-daily"
    },
    @{
        Created = (Join-Path $extraTasksRoot "created")
        Working = (Join-Path $extraTasksRoot "working")
        Finished = (Join-Path $extraTasksRoot "finished")
        Logs = (Join-Path $extraTasksRoot "logs")
        Label = "tasks"
    }
)

# Ensure all required directories exist
# Create them if they don't already exist
foreach ($root in $taskRoots) {
    foreach ($dir in @($root.Created, $root.Working, $root.Finished, $root.Logs)) {
        if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    }
}

# Initialize script-level logging
# Main log file for all script output
$scriptLogPath = Join-Path $logsDir "coworker-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
$scriptStartTime = Get-Date

# ============================================================================
# Logging Functions
# ============================================================================

# Function: Write message to console and main script log file
function Write-LogMessage {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,
        [ValidateSet('INFO', 'WARN', 'ERROR')]
        [string]$Level = 'INFO'
    )

    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $logEntry = "[$timestamp] [$Level] $Message"

    # Write to console
    switch ($Level) {
        'INFO' { Write-Host $logEntry }
        'WARN' { Write-Host $logEntry -ForegroundColor Yellow }
        'ERROR' { Write-Host $logEntry -ForegroundColor Red }
    }

    # Append to script log file
    $logEntry | Out-File -FilePath $scriptLogPath -Append
}

# Function: Write message only to log file (for verbose output)
function Write-LogVerbose {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message
    )

    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $logEntry = "[$timestamp] [DEBUG] $Message"

    # Append to script log file only (not console)
    $logEntry | Out-File -FilePath $scriptLogPath -Append
}

function Resolve-UniquePath {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Directory,
        [Parameter(Mandatory=$true)]
        [string]$BaseName,
        [Parameter(Mandatory=$true)]
        [string]$Extension
    )

    $candidateName = "$BaseName$Extension"
    $candidatePath = Join-Path $Directory $candidateName
    if (!(Test-Path $candidatePath)) {
        return @{ Path = $candidatePath; FileName = $candidateName }
    }

    $counter = 2
    while ($true) {
        $nextName = "$BaseName.$counter$Extension"
        $nextPath = Join-Path $Directory $nextName
        if (!(Test-Path $nextPath)) {
            return @{ Path = $nextPath; FileName = $nextName }
        }
        $counter++
    }
}

# Log script startup
Write-LogMessage "===========================================================================" INFO
Write-LogMessage "Coworker Task Runner - PowerShell Version" INFO
Write-LogMessage "Started at: $scriptStartTime" INFO
Write-LogMessage "Script Log: $scriptLogPath" INFO
Write-LogMessage "==========================================================================" INFO

foreach ($taskRoot in $taskRoots) {
    $createdDir = $taskRoot.Created
    $workingDir = $taskRoot.Working
    $finishedDir = $taskRoot.Finished
    $logsDir = $taskRoot.Logs

    $files = Get-ChildItem -Path $createdDir -File

    # Process each task file found in the created directory
    foreach ($file in $files) {
        Write-LogMessage "Processing $($file.Name)..." INFO

        # Read the entire file content
        $content = Get-Content -Path $file.FullName -Raw

        # Initialize variables for task metadata
        $title = ""
        $description = ""
        $prompt = ""

        # Try to parse structured content with Title, Description, and Prompt sections
        # Uses regex to extract these fields if they follow the expected format
        if ($content -match "(?ms)^Title:\s*(?<title>.*?)(\r\n|\n)Description:\s*(?<desc>.*?)(\r\n|\n)Prompt:\s*(?<prompt>.*)$") {
            $title = $Matches['title'].Trim()
            $description = $Matches['desc'].Trim()
            $prompt = $Matches['prompt'].Trim()
        } else {
            # Fallback: If file is not in structured format, use it as-is
            $title = $file.BaseName
            $description = "Task from $($file.Name)"
            $prompt = $content
        }

        # Sanitize the title to make it safe for use as a filename
        # Remove special characters that are not allowed in Windows filenames
        $safeTitle = $title -replace '[\\/*?:"<>|]', '_'
        if ([string]::IsNullOrWhiteSpace($safeTitle)) {
            $safeTitle = "task"
        }

        $taskInfo = Resolve-UniquePath -Directory $workingDir -BaseName $safeTitle -Extension $file.Extension
        $originalInfo = Resolve-UniquePath -Directory $workingDir -BaseName "$safeTitle.original" -Extension $file.Extension

        # Define full paths for the task file at each workflow stage
        $workingPath = $taskInfo.Path
        $workingOriginalPath = $originalInfo.Path

        $workingFileName = $taskInfo.FileName
        $workingBaseName = [System.IO.Path]::GetFileNameWithoutExtension($workingFileName)

        # Task log path - combined log for task execution
        $taskLogPath = Join-Path $logsDir "task_${workingBaseName}_$(Get-Date -Format 'yyyyMMdd-HHmmss').log"

        # Copilot-specific external tool log (separate from task log)
        $copilotLogPath = Join-Path $logsDir "copilot_${workingBaseName}_$(Get-Date -Format 'yyyyMMdd-HHmmss').log"

        $optimizedContent = @"
Title: $title
Description: $description
Prompt:
$prompt
"@

        # Move original task file from created directory to working directory (as .original)
        Move-Item -Path $file.FullName -Destination $workingOriginalPath -Force
        Write-LogMessage "Moved original to working: $workingOriginalPath" INFO

        # Write optimized task file to working directory
        $optimizedContent | Out-File -FilePath $workingPath -Encoding UTF8
        Write-LogMessage "Created optimized task: $workingPath" INFO
        Write-LogVerbose "Task log will be written to: $taskLogPath"

        # Change working directory to repository root
        # This ensures that Copilot runs in the correct context
        Push-Location $repoRoot

        Write-LogMessage "Executing Copilot for task: $title" INFO
        Write-LogVerbose "Task Description: $description"
        Write-LogVerbose "Prompt length: $($prompt.Length) characters"

        # Record task execution details to task log
        @"
Task: $title
Description: $description
Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Prompt:
$prompt
---
Copilot Execution Output:
"@ | Out-File -FilePath $taskLogPath

        try {
            # Escape double quotes in prompt for safe argument passing
            $promptEscaped = $prompt -replace '"', '\"'

            # Construct Copilot command arguments
            $copilotArgs = "-p `"$promptEscaped`" --allow-all-tools --allow-all-paths"

            # Define paths for temporary output and error logs (for copilot external tool)
            $stdOutLog = $copilotLogPath + ".stdout"
            $stdErrLog = $copilotLogPath + ".stderr"

            # Execute Copilot tool with the task prompt
            # Capture both standard output and error output to separate files
            $process = Start-Process -FilePath "copilot" -ArgumentList $copilotArgs -NoNewWindow -PassThru -RedirectStandardOutput $stdOutLog -RedirectStandardError $stdErrLog -Wait

            # Combine copilot stdout and stderr logs into the copilot-specific log
            # First append stdout if it exists
            if (Test-Path $stdOutLog) { Get-Content $stdOutLog | Out-File -FilePath $copilotLogPath -Append }
            # Then append stderr if it exists and contains content
            if (Test-Path $stdErrLog) {
                $errContent = Get-Content $stdErrLog
                if ($errContent) {
                    "`r`n=== COPILOT STDERR ===`r`n" | Out-File -FilePath $copilotLogPath -Append
                    $errContent | Out-File -FilePath $copilotLogPath -Append
                }
            }

            # Clean up temporary log files
            Remove-Item $stdOutLog -ErrorAction SilentlyContinue
            Remove-Item $stdErrLog -ErrorAction SilentlyContinue

            Write-LogMessage "Copilot execution finished with exit code $($process.ExitCode)" INFO
            Write-LogVerbose "Copilot external tool log: $copilotLogPath"

            # Append copilot result to task log
            @"

Copilot Exit Code: $($process.ExitCode)
Copilot Log: $copilotLogPath
"@ | Out-File -FilePath $taskLogPath -Append

            # Warn if Copilot exited with an error code
            if ($process.ExitCode -ne 0) {
                Write-LogMessage "Warning: Copilot exited with non-zero code. Check log: $copilotLogPath" WARN
            }
        }
        catch {
            # Handle any errors that occur during Copilot execution
            Write-LogMessage "Failed to execute copilot: $_" ERROR
            "Error executing copilot: $_" | Out-File -FilePath $taskLogPath -Append
        }
        finally {
            # Always return to the original directory after execution
            Pop-Location
        }

        # Move completed task from working directory to finished directory
        $finishedInfo = Resolve-UniquePath -Directory $finishedDir -BaseName $workingBaseName -Extension $file.Extension
        $finishedOriginalInfo = Resolve-UniquePath -Directory $finishedDir -BaseName "$safeTitle.original" -Extension $file.Extension

        Move-Item -Path $workingPath -Destination $finishedInfo.Path -Force
        Write-LogMessage "Task moved to finished: $($finishedInfo.Path)" INFO

        if (Test-Path $workingOriginalPath) {
            Move-Item -Path $workingOriginalPath -Destination $finishedOriginalInfo.Path -Force
            Write-LogMessage "Original moved to finished: $($finishedOriginalInfo.Path)" INFO
        }

        Write-LogMessage "---" INFO
    }
}

# Log script completion
$scriptEndTime = Get-Date
Write-LogMessage "===========================================================================" INFO
Write-LogMessage "All tasks completed" INFO
Write-LogMessage "Ended at: $scriptEndTime" INFO
Write-LogMessage "Script Log: $scriptLogPath" INFO
Write-LogMessage "==========================================================================" INFO

