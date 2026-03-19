@echo off
cd /d "%~dp0"
if not exist "service-account-key.json" (
    echo File not found: service-account-key.json
    echo Download key from Firebase Console, save it here as service-account-key.json
    pause
    exit /b 1
)
set GOOGLE_APPLICATION_CREDENTIALS=%~dp0service-account-key.json
node scripts/set-storage-cors.js
if %ERRORLEVEL% equ 0 echo Done. Avatar upload should work now.
pause
