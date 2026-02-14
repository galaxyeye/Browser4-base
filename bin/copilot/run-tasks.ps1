#!/usr/bin/env pwsh

# Run copilot to execute task described in `docs-dev/copilot/tasks/daily/tasks.md`

# Find the first parent directory that contains a VERSION file
$AppHome = (Get-Item -Path $MyInvocation.MyCommand.Path).Directory
while ($AppHome -ne $null -and -not (Test-Path (Join-Path $AppHome "VERSION"))) {
    $AppHome = Split-Path -Parent $AppHome
}
Set-Location $AppHome

$taskFilePath = Join-Path $AppHome "docs-dev\copilot\tasks\daily\tasks.md"
$prompt = "Run tasks described in $taskFilePath"

copilot -p "$prompt" --allow-all-tools
