# run_tasks.ps1

$baseDir = "D:\workspace\Browser4\Browser4-4.6\docs-dev\copilot\tasks\daily"
$createdDir = Join-Path $baseDir "created"
$workingDir = Join-Path $baseDir "working"
$finishedDir = Join-Path $baseDir "finished"
$repoRoot = "D:\workspace\Browser4\Browser4-4.6"

# Ensure directories exist
if (!(Test-Path $createdDir)) { New-Item -ItemType Directory -Path $createdDir | Out-Null }
if (!(Test-Path $workingDir)) { New-Item -ItemType Directory -Path $workingDir | Out-Null }
if (!(Test-Path $finishedDir)) { New-Item -ItemType Directory -Path $finishedDir | Out-Null }

$files = Get-ChildItem -Path $createdDir

foreach ($file in $files) {
    Write-Host "Processing $($file.Name)..."

    $content = Get-Content -Path $file.FullName -Raw

    $title = ""
    $description = ""
    $prompt = ""

    # Try to parse structured content
    # Format expected:
    # Title: ...
    # Description: ...
    # Prompt: ...
    if ($content -match "(?ms)^Title:\s*(?<title>.*?)(\r\n|\n)Description:\s*(?<desc>.*?)(\r\n|\n)Prompt:\s*(?<prompt>.*)$") {
        $title = $Matches['title'].Trim()
        $description = $Matches['desc'].Trim()
        $prompt = $Matches['prompt'].Trim()
    } else {
        # Fallback: Treat entire content as prompt
        $title = $file.BaseName
        $description = "Task from $($file.Name)"
        $prompt = $content
    }

    # Sanitize title for filename
    $safeTitle = $title -replace '[\\/*?:"<>|]', '_'
    $newFileName = "$safeTitle" + $file.Extension

    # Define paths
    $workingPath = Join-Path $workingDir $newFileName
    $finishedPath = Join-Path $finishedDir $newFileName
    $logPath = Join-Path $finishedDir ($newFileName + ".log")

    # Move to working directory (renaming if needed)
    Move-Item -Path $file.FullName -Destination $workingPath -Force
    Write-Host "Moved to working: $workingPath"

    # Change to repo root for execution
    Push-Location $repoRoot

    Write-Host "Executing Copilot for task: $title"
    Write-Host "Prompt: $prompt"

    try {
        # Prepare arguments
        # Escape double quotes in prompt
        $promptEscaped = $prompt -replace '"', '\"'

        # Construct single argument string to avoid PowerShell array quoting issues
        # format: -p "prompt" --allow-all-tools --allow-all-paths
        $copilotArgs = "-p `"$promptEscaped`" --allow-all-tools --allow-all-paths"

        $stdOutLog = $logPath + ".stdout"
        $stdErrLog = $logPath + ".stderr"

        # Execute and capture output
        # redirecting streams to separate files
        $process = Start-Process -FilePath "copilot" -ArgumentList $copilotArgs -NoNewWindow -PassThru -RedirectStandardOutput $stdOutLog -RedirectStandardError $stdErrLog -Wait

        # Combine logs
        if (Test-Path $stdOutLog) { Get-Content $stdOutLog | Out-File -FilePath $logPath -Append }
        if (Test-Path $stdErrLog) {
            $errContent = Get-Content $stdErrLog
            if ($errContent) {
                "`r`n=== STDERR ===`r`n" | Out-File -FilePath $logPath -Append
                $errContent | Out-File -FilePath $logPath -Append
            }
        }

        # Cleanup temp logs
        Remove-Item $stdOutLog -ErrorAction SilentlyContinue
        Remove-Item $stdErrLog -ErrorAction SilentlyContinue

        Write-Host "Copilot execution finished with exit code $($process.ExitCode)"

        if ($process.ExitCode -ne 0) {
            Write-Warning "Copilot exited with non-zero code. Check log: $logPath"
        }
    }
    catch {
        Write-Error "Failed to execute copilot: $_"
        "Error executing copilot: $_" | Out-File -FilePath $logPath -Append
    }
    finally {
        Pop-Location
    }

    # Move to finished
    Move-Item -Path $workingPath -Destination $finishedPath -Force
    Write-Host "Task moved to finished: $finishedPath"
    Write-Host "---------------------------------------------------"
}
