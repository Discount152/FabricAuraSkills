# AuraSkills: use Gradle caches on D: (avoids C: disk full during wrapper/dependency downloads)
#
# Usage (current PowerShell session only):
#   . .\scripts\use-gradle-on-d-drive.ps1
#
# Persist for your user (new terminals after reopening Cursor):
#   [Environment]::SetEnvironmentVariable("GRADLE_USER_HOME", "D:\gradle", "User")
# Then restart the terminal / IDE.

$gradleHome = "D:\gradle"
if (-not (Test-Path $gradleHome)) {
    New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null
}

$env:GRADLE_USER_HOME = $gradleHome
Write-Host "GRADLE_USER_HOME=$gradleHome (this session)"

# Optional: also point Java tools cache away from C: if you use other JDK builds
# $env:GRADLE_USER_HOME is the main fix for "Could not unzip ... gradle-*.zip ... not enough space"
