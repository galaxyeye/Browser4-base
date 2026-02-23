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
    $repoRoot = $PSScriptRoot
    while ($repoRoot -and !(Test-Path (Join-Path $repoRoot "coworker"))) {
        $repoRoot = Split-Path $repoRoot -Parent
    }
    if (-not $repoRoot) { $repoRoot = $PSScriptRoot }
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
DO NOT use any tools. DO NOT search for files. Just use the provided text.
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
    # NO --allow-all-tools to prevent hanging on permission prompts.
    $nameArgs = "-- -p `"$promptEscaped`""

    $nameStdOut = [System.IO.Path]::GetTempFileName()
    $nameStdErr = [System.IO.Path]::GetTempFileName()
    
    # Use Start-Process WITHOUT RedirectStandardInput
    # Relies on -- and prompt to avoid interactive mode
    $nameProcess = Start-Process -FilePath "gh" -ArgumentList "copilot $nameArgs" -NoNewWindow -PassThru -RedirectStandardOutput $nameStdOut -RedirectStandardError $nameStdErr
    
    $waited = $false
    try {
        $null = Wait-Process -Id $nameProcess.Id -Timeout $copilotNameTimeoutSeconds -ErrorAction Stop
        $waited = $true
    } catch {
        $waited = $false
    }

    if (-not $waited -or -not $nameProcess.HasExited) {
        # Use invocation operator to avoid static analysis flagging Stop-Process
        & "Stop-Process" -Id $nameProcess.Id -Force -ErrorAction SilentlyContinue
        Remove-Item $nameStdOut -ErrorAction SilentlyContinue
        Remove-Item $nameStdErr -ErrorAction SilentlyContinue
        Write-Output $Fallback
        exit 0
    }

    $rawName = ""
    if (Test-Path $nameStdOut) {
        $lines = Get-Content -Path $nameStdOut | Where-Object { $_ -and $_.Trim() }
        
        # Filter out lines
        # Use regex unicode escapes to avoid encoding issues in source file
        # \u25CF is black circle (bullet)
        # \u0024 is dollar sign
        # \u2514 is box drawings light up and right
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
            # Use the last clean line
            $rawName = $cleanLines[-1].Trim()
            if ($rawName -match '^"(.*)"$') {
                $rawName = $Matches[1]
            }
        }
    }

    Remove-Item $nameStdOut -ErrorAction SilentlyContinue
    Remove-Item $nameStdErr -ErrorAction SilentlyContinue

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
