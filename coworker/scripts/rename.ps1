param(
    [Parameter(Mandatory=$true)]
    [string]$FilePath
)

if (-not (Test-Path $FilePath)) {
    Write-Error "File not found: $FilePath"
    exit 1
}

$repoRoot = (git rev-parse --show-toplevel 2>$null)
if (-not $repoRoot) {
    Write-Host "Repo root not found. Exiting."
    exit 1
}
Set-Location $repoRoot

$content = Get-Content -Path $FilePath -Raw

# Initialize variables
$title = ""
$description = ""
$prompt = ""

# Parse structured content
if ($content -match "(?ms)^Title:\s*(?<title>.*?)(\r\n|\n)Description:\s*(?<desc>.*?)(\r\n|\n)Prompt:\s*(?<prompt>.*)$") {
    $title = $Matches['title'].Trim()
    $description = $Matches['desc'].Trim()
    $prompt = $Matches['prompt'].Trim()
} else {
    $fileItem = Get-Item $FilePath
    $title = $fileItem.BaseName
    $description = "Task from $($fileItem.Name)"
    $prompt = $content
}

$promptSample = $prompt
if ($promptSample.Length -gt 600) {
    $promptSample = $promptSample.Substring(0, 600)
}

$namingPrompt = @"
Create a short, descriptive task name in English kebab-case (3-6 words max). Output only the name.
Title: $title
Description: $description
Prompt: $promptSample
"@

$copilotNameTimeoutSeconds = 60
$Fallback = $title -replace '[\\/*?:"<>|]', '_'
if ([string]::IsNullOrWhiteSpace($Fallback)) {
    $Fallback = "task"
}

try {
    $promptEscaped = $namingPrompt -replace '"', '\"'
    # Use -- to separate gh flags from copilot flags. 
    $nameArgs = "-- -p `"$promptEscaped`""

    # Use temporary files for redirecting output
    $nameStdOut = [System.IO.Path]::GetTempFileName()
    $nameStdErr = [System.IO.Path]::GetTempFileName()
    
    # Create an empty file to use as input (simulate EOF/no input)
    $emptyInput = [System.IO.Path]::GetTempFileName()
    
    try {
        $nameProcess = Start-Process -FilePath "gh" -ArgumentList "copilot $nameArgs" -NoNewWindow -PassThru -RedirectStandardOutput $nameStdOut -RedirectStandardError $nameStdErr -RedirectStandardInput $emptyInput
    }
    catch {
        Write-Host "Error starting process: $_"
        # Fallback without RedirectStandardInput if it fails (e.g. PS version issues)
        $nameProcess = Start-Process -FilePath "gh" -ArgumentList "copilot $nameArgs" -NoNewWindow -PassThru -RedirectStandardOutput $nameStdOut -RedirectStandardError $nameStdErr
    }
    finally {
        # We can't delete emptyInput here yet because process is running, but we should clean it up later
    }

    $waited = $false
    try {
        $null = Wait-Process -Id $nameProcess.Id -Timeout $copilotNameTimeoutSeconds -ErrorAction Stop
        $waited = $true
    } catch {
        $waited = $false
    }

    if (-not $waited -or -not $nameProcess.HasExited) {
        Stop-Process -Id $nameProcess.Id -Force -ErrorAction SilentlyContinue
        Remove-Item $nameStdOut -ErrorAction SilentlyContinue
        Remove-Item $nameStdErr -ErrorAction SilentlyContinue
        Remove-Item $emptyInput -ErrorAction SilentlyContinue
        Write-Output $Fallback
        exit 0
    }

    $rawName = ""
    if (Test-Path $nameStdOut) {
        # The output from gh copilot includes descriptive text, the command, and execution output.
        # We want to extract the actual name which is likely echoed by the command or at the end.
        # Example output:
        # ● Echo task name
        #   $ echo "rename-task"
        #   ...
        # rename-task

        # Try to find the line that looks like a clean kebab-case name
        $lines = Get-Content -Path $nameStdOut | Where-Object { $_ -and $_.Trim() }
        
        # Look for the last line that is not part of the standard copilot UI output
        # Filter out lines starting with bullet points or other UI elements
        # Simplified regex to avoid encoding issues
        # Use explicit unicode escapes (ASCII-safe source)
        $cleanLines = @($lines | Where-Object { 
            $_ -notmatch '^\s*\u25CF' -and 
            $_ -notmatch '^\s*\u0024' -and 
            $_ -notmatch '^\s*\u2514' -and 
            $_ -notmatch '^error:' -and 
            $_ -notmatch '^Try ''copilot --help''' -and 
            $_ -notmatch '^Total' -and 
            $_ -notmatch '^API' -and 
            $_ -notmatch '^Breakdown' -and
            $_ -notmatch '^\s+gemini' -and
            $_ -notmatch '^\s+gpt' -and
            $_ -notmatch '^Days' -and
            $_ -notmatch '^Hours' -and
            $_ -notmatch '^Minutes' -and
            $_ -notmatch '^Seconds' -and
            $_ -notmatch '^Milliseconds' -and
            $_ -notmatch '^Ticks'
        })
        
        if ($cleanLines.Count -gt 0) {
            # Take the last clean line as it's likely the command output
            $rawName = $cleanLines[-1].Trim()
            
            # If the raw name is quoted, remove quotes
            if ($rawName -match '^"(.*)"$') {
                $rawName = $Matches[1]
            }
        }
    }

    Remove-Item $nameStdOut -ErrorAction SilentlyContinue
    Remove-Item $nameStdErr -ErrorAction SilentlyContinue
    Remove-Item $emptyInput -ErrorAction SilentlyContinue

    if ($nameProcess.ExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($rawName)) {
        Write-Output $Fallback
        exit 0
    }

    $normalized = $rawName.Trim()
    $normalized = $normalized -replace '\s+', '-'
    $normalized = $normalized -replace '[^A-Za-z0-9._-]', '-'
    $normalized = $normalized -replace '-+', '-'
    $normalized = $normalized.Trim(' ', '.', '-', '_')
    if ($normalized.Length -gt 60) {
        $normalized = $normalized.Substring(0, 60).Trim(' ', '.', '-', '_')
    }

    if ([string]::IsNullOrWhiteSpace($normalized)) {
        Write-Output $Fallback
    } else {
        Write-Output $normalized
    }
}
catch {
    Write-Output $Fallback
}
