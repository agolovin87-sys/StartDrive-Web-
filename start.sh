#!/bin/bash
set -e

WEBPACK_OUTPUT="webApp/build/kotlin-webpack/js/developmentExecutable/webApp.js"

if [ ! -f "$WEBPACK_OUTPUT" ]; then
  echo "Building Kotlin/JS webApp..."
  ./gradlew :webApp:jsBrowserDevelopmentWebpack --no-daemon --quiet
  echo "Build complete."
fi

echo "Starting web server on port 5000..."
node serve.js
