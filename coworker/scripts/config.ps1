$configDataPath = Join-Path $PSScriptRoot 'config.psd1'
if (-not (Test-Path $configDataPath)) {
    throw "Config data file not found: $configDataPath"
}

$configData = Import-PowerShellDataFile -Path $configDataPath
if (-not $configData.ContainsKey('COPILOT')) {
    throw "COPILOT is not defined in $configDataPath"
}

$COPILOT = @($configData['COPILOT'])
