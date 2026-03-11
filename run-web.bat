@echo off
title StartDrive Web
cd /d "%~dp0"

if not defined JAVA_HOME (
  set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
  if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA not found. Install JDK 11+ or set JAVA_HOME to your JDK.
    echo Example: set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    pause
    exit /b 1
  )
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Checking port 8081...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (taskkill /PID %%a /F 2>nul & goto :port_freed)
:port_freed
echo Starting web app (first run may take 1-2 min)...
call gradlew.bat :webApp:jsBrowserDevelopmentRun
if errorlevel 1 (
  echo.
  echo Build or run failed. Check errors above.
) else (
  echo.
  echo If browser did not open: http://localhost:8081/
)
pause
