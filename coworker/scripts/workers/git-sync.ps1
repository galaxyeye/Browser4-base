#!/usr/bin/env pwsh

# 🔍 Find repo root
$repoRoot = git rev-parse --show-toplevel 2>$null
if (-not $repoRoot) {
    Write-Host "Repo root not found. Exiting."
    exit 1
}

Set-Location $repoRoot

$ghCopilotHelper = Join-Path $repoRoot "coworker\scripts\workers\gh-copilot.ps1"
. $ghCopilotHelper
$copilotCommand = Get-GHCopilotCommand -RepoRoot $repoRoot

$prompt = @"
Commit all changes in "$repoRoot".
Pull from remote.
Then push to remote.
If conflicts occur, resolve them automatically.
"@

$copilotArguments = New-GHCopilotArguments -BaseArgs $copilotCommand.BaseArgs -Prompt $prompt -AdditionalArguments @('--allow-all-tools')

Write-Host "Running:"
Write-Host (Format-GHCopilotCommand -Executable $copilotCommand.Executable -Arguments $copilotArguments)

Invoke-GHCopilot -Prompt $prompt -AdditionalArguments @('--allow-all-tools')
exit $LASTEXITCODE
