@echo off
title StartDrive Web
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
echo Starting web app...
call gradlew.bat :webApp:jsBrowserDevelopmentRun
pause
