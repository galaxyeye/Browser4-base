#!/usr/bin/env pwsh

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (git rev-parse --show-toplevel 2>$null)

if ([string]::IsNullOrWhiteSpace($repoRoot)) {
    $repoRoot = $scriptDir
    while (-not (Test-Path (Join-Path $repoRoot 'VERSION'))) {
        $parent = Split-Path -Parent $repoRoot
        if ($parent -eq $repoRoot) {
            break
        }

        $repoRoot = $parent
    }
}

if (-not (Test-Path (Join-Path $repoRoot 'VERSION'))) {
    Write-Error "Could not locate the repository root from $scriptDir"
    exit 1
}

Set-Location $repoRoot

function Print-Usage {
    Write-Host "Usage: test.ps1 [test-types...] [maven-args...]"
    Write-Host ""
    Write-Host "Test Types:"
    Write-Host "  fast        Run fast unit tests only"
    Write-Host "  it          Run integration tests"
    Write-Host "  e2e         Run end-to-end tests"
    Write-Host "  nodejs-sdk  Run Browser4 CLI tests from sdks\browser4-cli"
    Write-Host "  core        Run core module supplementary tests"
    Write-Host "  rest        Run REST module tests"
    Write-Host "  skills      Run skills-focused agentic tests"
    Write-Host "  mcp         Run MCP-focused agentic tests"
    Write-Host "  browser4    Run all Browser4 main tests (fast, core, rest, it, e2e)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  test.ps1 fast                       # Run fast unit tests"
    Write-Host "  test.ps1 it                         # Run integration tests"
    Write-Host "  test.ps1 e2e                        # Run end-to-end tests"
    Write-Host "  test.ps1 nodejs-sdk                 # Run Browser4 CLI tests"
    Write-Host "  test.ps1 nodejs-sdk -- --coverage   # Run Browser4 CLI tests with coverage"
    Write-Host "  test.ps1 skills                     # Run skills-focused agentic tests"
    Write-Host "  test.ps1 mcp                        # Run MCP-focused agentic tests"
    Write-Host "  test.ps1 browser4                   # Run all Browser4 main tests"
    Write-Host '  test.ps1 it -pl pulsar-core         # Pass additional Maven args through'
    exit 1
}

function Exit-RemovedTestType([string]$testType) {
    Write-Error "Test type '$testType' is no longer available in this checkout because the corresponding module was removed. Use 'nodejs-sdk' for sdks\browser4-cli tests."
    exit 1
}

function Invoke-MavenTests([string[]]$testTypes, [string[]]$additionalMvnArgs) {
    $mvnCmd = Join-Path $repoRoot 'mvnw.cmd'
    if (-not (Test-Path $mvnCmd)) {
        Write-Error "Maven wrapper not found at $mvnCmd"
        exit 1
    }

    Write-Host "=========================================="
    Write-Host "Running Maven tests: $($testTypes -join ', ')"
    Write-Host "=========================================="

    $mvnTestArgs = @('test', '-P=-examples')

    $hasFast = $testTypes -contains 'fast'
    $hasIT = $testTypes -contains 'it'
    $hasE2E = $testTypes -contains 'e2e'
    $hasCore = $testTypes -contains 'core'
    $hasRest = $testTypes -contains 'rest'
    $hasSkills = $testTypes -contains 'skills'
    $hasMcp = $testTypes -contains 'mcp'

    if ($hasIT) { $mvnTestArgs += '-DrunITs=true' }
    if ($hasE2E) { $mvnTestArgs += '-DrunE2ETests=true' }
    if ($hasCore) {
        $mvnTestArgs += '-DrunCoreTests=true'
        $mvnTestArgs += '-Ppulsar-core-tests'
    }

    $modules = @()
    if ($hasCore) {
        $modules += @(
            'pulsar-core/pulsar-resources',
            'pulsar-core/pulsar-common',
            'pulsar-core/pulsar-dom',
            'pulsar-core/pulsar-persist',
            'pulsar-core/pulsar-plugins',
            'pulsar-core/pulsar-third',
            'pulsar-core/pulsar-skeleton',
            'pulsar-core/pulsar-browser',
            'pulsar-core/pulsar-spring-support',
            'pulsar-core/pulsar-ql-common',
            'pulsar-core/pulsar-ql',
            'pulsar-core/pulsar-core-tests',
            'pulsar-core/pulsar-core-tests/pulsar-common-tests',
            'pulsar-core/pulsar-core-tests/pulsar-dom-tests',
            'pulsar-core/pulsar-core-tests/pulsar-ql-tests'
        )
    }

    if ($hasSkills -or $hasMcp) {
        $modules += 'pulsar-agentic'

        if (-not ($hasFast -or $hasIT -or $hasE2E -or $hasCore -or $hasRest)) {
            $patterns = @()
            if ($hasSkills) { $patterns += '*Skill*' }
            if ($hasMcp) { $patterns += '*MCP*' }

            if ($patterns.Count -gt 0) {
                $mvnTestArgs += "-Dtest=$($patterns -join ',')"
                $mvnTestArgs += '-Dsurefire.failIfNoSpecifiedTests=false'
            }
        }
    }

    if ($hasFast -or $hasRest) {
        $modules = @()
    }

    if ($modules.Count -gt 0) {
        $mvnTestArgs += '-pl'
        $mvnTestArgs += ($modules -join ',')
        $mvnTestArgs += '-am'
    }

    $mvnTestArgs += $additionalMvnArgs

    try {
        & $mvnCmd @mvnTestArgs
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) {
            Write-Host ""
            Write-Host "=========================================="
            Write-Host "❌ Maven tests failed with exit code $exitCode"
            Write-Host "=========================================="
            exit $exitCode
        }

        Write-Host ""
        Write-Host "=========================================="
        Write-Host "✅ Maven tests completed successfully"
        Write-Host "=========================================="
    }
    catch {
        Write-Error "Failed to execute Maven tests: $_"
        exit 1
    }
}

function Invoke-NodejsSdkTests([string[]]$additionalArgs) {
    $nodejsSdkDir = Join-Path $repoRoot 'sdks\browser4-cli'

    Write-Host "=========================================="
    Write-Host "Running nodejs-sdk tests..."
    Write-Host "=========================================="

    if (-not (Test-Path $nodejsSdkDir)) {
        Write-Error "Browser4 CLI directory not found at $nodejsSdkDir"
        exit 1
    }

    $nodeCmd = Get-Command node -ErrorAction SilentlyContinue
    if (-not $nodeCmd) {
        Write-Error "Node.js is not installed or not in PATH"
        exit 1
    }

    Push-Location $nodejsSdkDir
    try {
        Write-Host "Working directory: $(Get-Location)"

        if (-not (Test-Path "$nodejsSdkDir\node_modules")) {
            Write-Host "Installing dependencies..."
            & npm install
            if ($LASTEXITCODE -ne 0) {
                Write-Error "Failed to install dependencies"
                exit $LASTEXITCODE
            }
        }

        if (-not (Test-Path "$nodejsSdkDir\node_modules\.bin\jest.cmd")) {
            Write-Error "jest is not installed. Install it with: npm install"
            exit 1
        }

        $npmArgs = @('test', '--') + $additionalArgs
        & npm @npmArgs
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) {
            Write-Host ""
            Write-Host "=========================================="
            Write-Host "❌ nodejs-sdk tests failed with exit code $exitCode"
            Write-Host "=========================================="
            exit $exitCode
        }

        Write-Host ""
        Write-Host "=========================================="
        Write-Host "✅ nodejs-sdk tests completed successfully"
        Write-Host "=========================================="
    }
    catch {
        Write-Error "Failed to execute nodejs-sdk tests: $_"
        exit 1
    }
    finally {
        Pop-Location
    }
}

$knownTestTypes = @('fast', 'it', 'e2e', 'nodejs-sdk', 'core', 'rest', 'skills', 'mcp', 'browser4')
$removedTestTypes = @('kotlin-sdk', 'python-sdk')
$testTypes = @()
$additionalMvnArgs = @()
$parsingTestTypes = $true

if ($args.Count -eq 0) {
    Print-Usage
}

foreach ($arg in $args) {
    if ($arg -in '-h', '-help', '--help') {
        Print-Usage
    }

    if ($arg -in $removedTestTypes) {
        Exit-RemovedTestType $arg
    }

    if ($parsingTestTypes -and ($arg -in $knownTestTypes)) {
        $testTypes += $arg
    }
    else {
        $parsingTestTypes = $false
        $additionalMvnArgs += $arg
    }
}

if ($testTypes.Count -eq 0) {
    $testTypes += 'fast'
}

$mavenTests = @()
$sdkTests = @()

foreach ($type in $testTypes) {
    if ($type -eq 'browser4') {
        $mavenTests += 'fast', 'core', 'it', 'e2e', 'rest'
        continue
    }

    if ($type -eq 'nodejs-sdk') {
        $sdkTests += $type
        continue
    }

    $mavenTests += $type
}

$mavenTests = $mavenTests | Select-Object -Unique
$sdkTests = $sdkTests | Select-Object -Unique

if ($mavenTests.Count -gt 0) {
    Invoke-MavenTests -testTypes $mavenTests -additionalMvnArgs $additionalMvnArgs
}

if ($sdkTests -contains 'nodejs-sdk') {
    Invoke-NodejsSdkTests -additionalArgs $additionalMvnArgs
}

exit 0
