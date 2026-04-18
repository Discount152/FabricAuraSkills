@echo off
setlocal
cd /d "%~dp0.."
call gradlew.bat -PfabricOnly=true :fabric:runClient %*
exit /b %ERRORLEVEL%
