<#
.SYNOPSIS
    Builds the mod jar with Gradle and copies it into a Minecraft mods folder.

.PARAMETER ModsDir
    Destination mods folder. Defaults to the PrismLauncher 1.21.1 instance.

.PARAMETER KeepOld
    Keep older builds of this mod in the mods folder instead of removing them.

.PARAMETER Clean
    Run 'gradlew clean' before building.
#>
param(
    [string]$ModsDir = "$env:APPDATA\PrismLauncher\instances\1.21.1\minecraft\mods",
    [switch]$KeepOld,
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

# Read mod_id and mod_version from gradle.properties
$props = @{}
Get-Content (Join-Path $root 'gradle.properties') | ForEach-Object {
    if ($_ -match '^\s*([^#=\s]+)\s*=\s*(.*)$') { $props[$Matches[1]] = $Matches[2].Trim() }
}
$modId = $props['mod_id']
$version = $props['mod_version']
$jarName = "$modId-$version.jar"

# Build
$tasks = @()
if ($Clean) { $tasks += 'clean' }
$tasks += 'jar'
Write-Host "Building $jarName ..." -ForegroundColor Cyan
Push-Location $root
try {
    & (Join-Path $root 'gradlew.bat') @tasks
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit code $LASTEXITCODE)." }
} finally {
    Pop-Location
}

$jar = Join-Path $root "build\libs\$jarName"
if (-not (Test-Path $jar)) { throw "Built jar not found: $jar" }

# Deploy
if (-not (Test-Path $ModsDir)) {
    New-Item -ItemType Directory -Path $ModsDir -Force | Out-Null
}

if (-not $KeepOld) {
    Get-ChildItem -Path $ModsDir -Filter "$modId-*.jar" |
        Where-Object { $_.Name -ne $jarName } |
        ForEach-Object {
            Write-Host "Removing old build: $($_.Name)" -ForegroundColor Yellow
            Remove-Item $_.FullName -Force
        }
}

Copy-Item -Path $jar -Destination $ModsDir -Force
Write-Host "Deployed $jarName -> $ModsDir" -ForegroundColor Green
