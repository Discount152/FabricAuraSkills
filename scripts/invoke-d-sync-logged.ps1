# Wrapper: powershell -File .\scripts\invoke-d-sync-logged.ps1 (no inline $vars — safe for nested shells)
$repo = Split-Path $PSScriptRoot -Parent
$log = Join-Path $repo "agent-d-drive-run.log"
Remove-Item $log -ErrorAction SilentlyContinue
& (Join-Path $PSScriptRoot "sync-to-d-and-gradle-here.ps1") *>&1 | Tee-Object -FilePath $log
exit $LASTEXITCODE
