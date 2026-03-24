param()

$ErrorActionPreference = "Stop"

function Resolve-JavaHome {
    $candidates = @(
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Eclipse Adoptium\jdk-21",
        "C:\Program Files\Java\jdk-21"
    )
    foreach ($path in $candidates) {
        if (Test-Path (Join-Path $path "bin\java.exe")) {
            return $path
        }
    }
    return $null
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaHome = Resolve-JavaHome
if (-not $javaHome) {
    throw "JAVA_HOME not found. Install JDK or Android Studio (JBR)."
}

$env:JAVA_HOME = $javaHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
Write-Host "Building web bundle..."
& .\gradlew.bat :webApp:jsBrowserProductionWebpack

$srcDir = Join-Path $projectRoot "webApp\build\kotlin-webpack\js\productionExecutable"
$dstDir = Join-Path $projectRoot "webApp\build\processedResources\js\main"

if (-not (Test-Path $srcDir)) {
    throw "Source bundle dir not found: $srcDir"
}
if (-not (Test-Path $dstDir)) {
    New-Item -ItemType Directory -Path $dstDir | Out-Null
}

Write-Host "Copying webApp.js artifacts..."
Copy-Item (Join-Path $srcDir "webApp.js*") $dstDir -Force

Write-Host "Deploying to Firebase Hosting..."
& firebase deploy --only hosting

Write-Host "Done. Hosting URL: https://startdrive-573fa.web.app"
