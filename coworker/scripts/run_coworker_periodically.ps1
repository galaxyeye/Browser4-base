#!/usr/bin/env pwsh

$repoRoot = (git rev-parse --show-toplevel 2>$null)
if (-not $repoRoot) {
    Write-Host "Repo root not found. Exiting."
    exit 1
}
Set-Location $repoRoot

$ScriptPath = Resolve-Path ".\coworker\scripts\coworker.ps1"
$ScriptName = "coworker.ps1"

Write-Host "Monitoring $ScriptName..."
Write-Host "Script path: $ScriptPath"

while ($true) {
    $Running = Get-CimInstance Win32_Process | Where-Object {
        ($_.Name -eq 'pwsh.exe' -or $_.Name -eq 'powershell.exe') -and
        $_.CommandLine -like "*$ScriptName*" -and
        $_.CommandLine -notlike "*run_coworker_periodically.ps1*" -and
        $_.ProcessId -ne $PID
    }

    if ($Running) {
        Write-Host "$ScriptName is already running."
    } else {
        Write-Host "$ScriptName is NOT running. Starting it..."
        try {
            # Start the process
            Start-Process pwsh -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$ScriptPath`"" -WorkingDirectory (Split-Path $ScriptPath)
            Write-Host "Started $ScriptName."
        } catch {
            Write-Error "Failed to start ${ScriptName}: $_"
        }
    }

    Start-Sleep -Seconds 15
}
