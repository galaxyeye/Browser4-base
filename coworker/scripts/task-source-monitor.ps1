#!/usr/bin/env pwsh

$ErrorActionPreference = 'Stop'

function Get-RepoRoot {
    $currentDirectory = $PSScriptRoot
    while ($currentDirectory) {
        if ((Test-Path (Join-Path $currentDirectory 'ROOT.md')) -or (Test-Path (Join-Path $currentDirectory '.git'))) {
            return (Resolve-Path $currentDirectory).Path
        }

        $parentDirectory = Split-Path -Parent $currentDirectory
        if ($parentDirectory -eq $currentDirectory) {
            break
        }
        $currentDirectory = $parentDirectory
    }

    throw 'Repo root not found.'
}

$repoRoot = Get-RepoRoot
$target = Join-Path $repoRoot 'coworker\scripts\deprecated\task-source-monitor.ps1'

Write-Warning 'task-source-monitor.ps1 is deprecated as a scheduler entry point. Use coworker\scripts\coworker-scheduler.ps1 for recurring runs.'
& $target @args
exit $LASTEXITCODE

