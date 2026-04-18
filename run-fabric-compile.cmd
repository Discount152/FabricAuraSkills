@echo off
set GRADLE_USER_HOME=D:\gradle
set TEMP=D:\gradle-temp
set TMP=D:\gradle-temp
if not exist "%TEMP%" mkdir "%TEMP%"
cd /d "%~dp0"
call gradlew.bat -PfabricOnly=true :fabric:compileJava > build-fabric-compile.log 2>&1
exit /b %ERRORLEVEL%
