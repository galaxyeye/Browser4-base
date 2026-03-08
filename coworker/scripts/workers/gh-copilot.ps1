#!/usr/bin/env pwsh

param(
    [string]$Prompt,
    [string[]]$AdditionalArguments = @(),
    [switch]$AllowAllTools,
    [switch]$AllowAllPaths,
    [switch]$CaptureOutput
)

function Get-GHCopilotRepoRoot {
    param(
        [string]$StartDirectory = $PSScriptRoot
    )

    $repoRoot = git rev-parse --show-toplevel 2>$null
    if ($repoRoot) {
        return (Resolve-Path $repoRoot).Path
    }

    $currentDirectory = $StartDirectory
    while ($currentDirectory) {
        if (Test-Path (Join-Path $currentDirectory 'ROOT.md')) {
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

function Get-GHCopilotCommand {
    param(
        [string]$RepoRoot = (Get-GHCopilotRepoRoot)
    )

    $configPath = Join-Path $RepoRoot 'coworker\scripts\config.ps1'
    if (Test-Path $configPath) {
        . $configPath
    }

    if (-not $COPILOT) {
        $COPILOT = @('gh', 'copilot')
    }

    if ($COPILOT -is [string]) {
        throw "COPILOT must be defined as a PowerShell array in $configPath"
    }

    if ($COPILOT.Count -lt 2) {
        throw 'COPILOT must include an executable and at least one argument'
    }

    return [pscustomobject]@{
        RepoRoot   = $RepoRoot
        ConfigPath = $configPath
        Executable = $COPILOT[0]
        BaseArgs   = @($COPILOT | Select-Object -Skip 1)
    }
}

function New-GHCopilotArguments {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$BaseArgs,
        [string]$Prompt,
        [string[]]$AdditionalArguments = @()
    )

    $arguments = @($BaseArgs)
    if ($PSBoundParameters.ContainsKey('Prompt')) {
        $arguments += '--'
        $arguments += '-p'
        $arguments += $Prompt
    }

    if ($AdditionalArguments) {
        $arguments += $AdditionalArguments
    }

    return @($arguments)
}

function Format-GHCopilotCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $formattedArguments = foreach ($argument in $Arguments) {
        if ([string]::IsNullOrEmpty($argument)) {
            "''"
        }
        elseif ($argument -match '[\s"`]') {
            "'" + ($argument -replace "'", "''") + "'"
        }
        else {
            $argument
        }
    }

    return ('{0} {1}' -f $Executable, ($formattedArguments -join ' ')).Trim()
}

function ConvertTo-WindowsCommandLineArgument {
    param(
        [AllowEmptyString()]
        [string]$Argument
    )

    if ($null -eq $Argument -or $Argument.Length -eq 0) {
        return '""'
    }

    if ($Argument -notmatch '[\s"]') {
        return $Argument
    }

    $builder = [System.Text.StringBuilder]::new()
    [void]$builder.Append('"')

    $backslashCount = 0
    foreach ($character in $Argument.ToCharArray()) {
        if ($character -eq '\') {
            $backslashCount++
            continue
        }

        if ($character -eq '"') {
            if ($backslashCount -gt 0) {
                [void]$builder.Append('\' * ($backslashCount * 2))
                $backslashCount = 0
            }
            [void]$builder.Append('\"')
            continue
        }

        if ($backslashCount -gt 0) {
            [void]$builder.Append('\' * $backslashCount)
            $backslashCount = 0
        }

        [void]$builder.Append($character)
    }

    if ($backslashCount -gt 0) {
        [void]$builder.Append('\' * ($backslashCount * 2))
    }

    [void]$builder.Append('"')
    return $builder.ToString()
}

function Start-GHCopilotProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$BaseArgs,
        [string]$Prompt,
        [string[]]$AdditionalArguments = @(),
        [string]$StdOutPath,
        [string]$StdErrPath,
        [switch]$NoNewWindow
    )

    $arguments = New-GHCopilotArguments -BaseArgs $BaseArgs -Prompt $Prompt -AdditionalArguments $AdditionalArguments
    $startProcessArgs = @{
        FilePath = $Executable
        PassThru = $true
    }

    $isWindowsPlatform = $false
    if ($null -ne $PSVersionTable -and $PSVersionTable.PSEdition -eq 'Desktop') {
        $isWindowsPlatform = $true
    }
    elseif ($null -ne (Get-Variable -Name IsWindows -ErrorAction SilentlyContinue)) {
        $isWindowsPlatform = [bool]$IsWindows
    }

    if ($isWindowsPlatform) {
        # Use one escaped command line on Windows to preserve multiline/quoted prompt text.
        $escapedArguments = foreach ($argument in $arguments) {
            ConvertTo-WindowsCommandLineArgument -Argument $argument
        }
        $startProcessArgs.ArgumentList = ($escapedArguments -join ' ')
    }
    else {
        $startProcessArgs.ArgumentList = $arguments
    }

    if ($NoNewWindow) {
        $startProcessArgs.NoNewWindow = $true
    }
    if ($PSBoundParameters.ContainsKey('StdOutPath')) {
        $startProcessArgs.RedirectStandardOutput = $StdOutPath
    }
    if ($PSBoundParameters.ContainsKey('StdErrPath')) {
        $startProcessArgs.RedirectStandardError = $StdErrPath
    }

    return Start-Process @startProcessArgs
}

function Invoke-GHCopilot {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Prompt,
        [string[]]$AdditionalArguments = @(),
        [string]$RepoRoot = (Get-GHCopilotRepoRoot)
    )

    $command = Get-GHCopilotCommand -RepoRoot $RepoRoot
    $arguments = New-GHCopilotArguments -BaseArgs $command.BaseArgs -Prompt $Prompt -AdditionalArguments $AdditionalArguments
    & $command.Executable @arguments
}

if ($MyInvocation.InvocationName -ne '.') {
    if ([string]::IsNullOrWhiteSpace($Prompt)) {
        throw 'Prompt is required when executing gh-copilot.ps1 directly.'
    }

    $directArguments = @($AdditionalArguments)
    if ($AllowAllTools) {
        $directArguments += '--allow-all-tools'
    }
    if ($AllowAllPaths) {
        $directArguments += '--allow-all-paths'
    }

    if ($CaptureOutput) {
        Invoke-GHCopilot -Prompt $Prompt -AdditionalArguments $directArguments
        exit $LASTEXITCODE
    }

    $command = Get-GHCopilotCommand
    $process = Start-GHCopilotProcess -Executable $command.Executable -BaseArgs $command.BaseArgs -Prompt $Prompt -AdditionalArguments $directArguments -NoNewWindow
    $process.WaitForExit()
    exit $process.ExitCode
}
