# Copies this repo to D:\Projects\AuraSkills (excludes build/.gradle), sets GRADLE_USER_HOME=D:\gradle,
# then runs Gradle from the D: copy. Use when C: is low on space.
#
# Run from repo root:
#   powershell -ExecutionPolicy Bypass -File .\scripts\sync-to-d-and-gradle-here.ps1
#
# Optional:
#   -SkipCopy              use existing D:\Projects\AuraSkills (no robocopy)
#   -GradleTask "tasks"    task list passed to gradlew (default :fabric:compileJava)

param(
    [switch] $SkipCopy,
    [string] $GradleTask = ":fabric:compileJava"
)

$ErrorActionPreference = "Stop"
# scripts\ lives directly under the Gradle project root (AuraSkills\)
$repoRoot = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path (Join-Path $repoRoot "gradlew.bat"))) {
    Write-Error "Could not find gradlew.bat next to scripts\. Run from the AuraSkills repo root layout."
}

$dest = "D:\Projects\AuraSkills"
$gradleHome = "D:\gradle"

New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null
New-Item -ItemType Directory -Force -Path "D:\Projects" | Out-Null
$env:GRADLE_USER_HOME = $gradleHome
$tempD = "D:\gradle-temp"
New-Item -ItemType Directory -Force -Path $tempD | Out-Null
$env:TEMP = $tempD
$env:TMP = $tempD
Write-Host "GRADLE_USER_HOME=$gradleHome TEMP/TMP=$tempD"

if (-not $SkipCopy) {
    Write-Host "Robocopy $repoRoot -> $dest (excluding build, .gradle; extra files on D: are left in place) ..."
    $robolog = Join-Path $env:TEMP "auraskills-robocopy.log"
    # Exclude transient logs that may be locked or recreated while this script runs
    & robocopy $repoRoot $dest /E /R:2 /W:2 /XD build .gradle /XF agent-d-drive-run.log /LOG:$robolog
    $code = $LASTEXITCODE
    if ($code -ge 8) {
        Write-Error "robocopy failed (exit $code). See $robolog"
    }
    Write-Host "Robocopy finished (exit $code). Log: $robolog"
}

$gw = Join-Path $dest "gradlew.bat"
if (-not (Test-Path $gw)) {
    Write-Error "Missing $gw - run without -SkipCopy first."
}

Push-Location $dest
try {
    Write-Host "Running: gradlew.bat $GradleTask"
    $ga = @($GradleTask -split '\s+' | Where-Object { $_ })
    & .\gradlew.bat @ga
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
