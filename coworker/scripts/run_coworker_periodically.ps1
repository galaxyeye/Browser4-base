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
    $createdTasks = Get-ChildItem -Path ".\coworker\tasks\1created" -File -ErrorAction SilentlyContinue
    $approvedTasks = Get-ChildItem -Path ".\coworker\tasks\5approved" -File -ErrorAction SilentlyContinue

    if (-not ($createdTasks -or $approvedTasks)) {
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Write-Host "$timestamp - No tasks found in 1created or 5approved. Skipping check."
        Start-Sleep -Seconds 15
        continue
    }

    $Running = Get-CimInstance Win32_Process | Where-Object {
        ($_.Name -eq 'pwsh.exe' -or $_.Name -eq 'powershell.exe') -and
        $_.CommandLine -like "*$ScriptName*" -and
        $_.CommandLine -notlike "*run_coworker_periodically.ps1*" -and
        $_.ProcessId -ne $PID
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    if ($Running) {
        Write-Host "$timestamp - $ScriptName is already running."
    } else {
        Write-Host "$timestamp - $ScriptName is NOT running. Starting it..."
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
