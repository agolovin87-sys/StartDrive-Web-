@echo off
title StartDrive Web — production build
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

echo Building production bundle...
call gradlew.bat :webApp:jsBrowserProductionWebpack --no-daemon
if errorlevel 1 ( echo Build failed. & pause & exit /b 1 )

set "RES=webApp\build\processedResources\js\main"
set "OUT=webApp\build\kotlin-webpack\js\productionExecutable"
if exist "%RES%" (
  echo Copying index.html, style.css, firebase-config.js and assets...
  xcopy /Y /Q "%RES%\*" "%OUT%\"
  echo.
  echo Done. Deploy-ready folder: %OUT%
) else (
  echo Warning: processedResources not found, copy skipped.
)

echo.
echo To deploy: firebase deploy --only hosting
pause
