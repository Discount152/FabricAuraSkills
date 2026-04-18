# Run gradlew from D:\Projects\AuraSkills with GRADLE_USER_HOME=D:\gradle (avoids nested-shell $env bugs).
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $GradleArgs = $null
)
if (-not $GradleArgs -or $GradleArgs.Count -eq 0) {
    $GradleArgs = @(":fabric:compileJava")
}
$env:GRADLE_USER_HOME = "D:\gradle"
$tempD = "D:\gradle-temp"
New-Item -ItemType Directory -Force -Path $tempD | Out-Null
$env:TEMP = $tempD
$env:TMP = $tempD
$root = "D:\Projects\AuraSkills"
if (-not (Test-Path (Join-Path $root "gradlew.bat"))) {
    Write-Error "Missing D:\Projects\AuraSkills — run scripts\sync-to-d-and-gradle-here.ps1 first."
}
Push-Location $root
try {
    Write-Host "D:\Projects\AuraSkills> gradlew.bat $($GradleArgs -join ' ')"
    & .\gradlew.bat @GradleArgs
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
