@echo off

REM Fixes "Failed to create Jar file ... instrumented-*.jar" / ExternalDependencyInstrumentingArtifactTransform

REM by removing Gradle's instrumentation transform outputs (safe; they are regenerated).

set "GVER=8.14.2"

if exist "%USERPROFILE%\.gradle\caches\%GVER%\transforms" (

  echo Removing "%USERPROFILE%\.gradle\caches\%GVER%\transforms"

  rmdir /s /q "%USERPROFILE%\.gradle\caches\%GVER%\transforms"

  echo Done.

) else (

  echo No transforms folder at caches\%GVER%\transforms — nothing to do.

)

exit /b 0

