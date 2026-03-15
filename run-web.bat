@echo off
setlocal enabledelayedexpansion
title StartDrive Web
cd /d "%~dp0"

if not defined JAVA_HOME (
  set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
  if exist "!JAVA_HOME!\bin\java.exe" goto java_ok
  set "JAVA_HOME=C:\Program Files\Android\Android Studio\jre"
  if exist "!JAVA_HOME!\bin\java.exe" goto java_ok
  set "JAVA_HOME=C:\Program Files\Java\jdk-17"
  if exist "!JAVA_HOME!\bin\java.exe" goto java_ok
  set "JAVA_HOME=C:\Program Files\Java\jdk-21"
  if exist "!JAVA_HOME!\bin\java.exe" goto java_ok
  set "JAVA_HOME=C:\Program Files\Java\jdk-11"
  if exist "!JAVA_HOME!\bin\java.exe" goto java_ok
  echo JAVA not found. Install JDK 11+ or set JAVA_HOME before running.
  echo.
  echo If Android Studio is installed, run in cmd:
  echo   set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
  echo   run-web.bat
  echo.
  pause
  exit /b 1
)
:java_ok
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
