@echo off
setlocal
cd /d "%~dp0.."
call gradlew.bat -PfabricOnly=true :fabric:build %*
exit /b %ERRORLEVEL%
