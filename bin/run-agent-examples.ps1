#!/usr/bin/env pwsh

$repoRoot = (git rev-parse --show-toplevel 2>$null)
Set-Location $repoRoot

# Import common utility script
. $repoRoot\bin\common\Util.ps1

Fix-Encoding-UTF8

# 运行
$jarPattern = "$repoRoot\examples\browser4-examples\target\browser4-examples-*.jar"
$jarPath = Get-ChildItem $jarPattern | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1

if ($null -eq $jarPath -or -not (Test-Path $jarPath)) {
    Write-Host "JAR file not found matching $jarPattern"
    Write-Host "Building it now..."
    ./mvnw.cmd clean package -pl examples/browser4-examples -DskipTests
    
    $jarPath = Get-ChildItem $jarPattern | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
    if ($null -eq $jarPath) {
        Write-Error "Build failed or JAR not found."
        exit 1
    }
}

Write-Host "Running $jarPath..."
java -D"file.encoding=UTF-8" -jar $jarPath.FullName
