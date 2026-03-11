#!/bin/bash
set -e

WEBPACK_OUTPUT="webApp/build/kotlin-webpack/js/developmentExecutable/webApp.js"

if [ ! -f "$WEBPACK_OUTPUT" ]; then
  echo "Building Kotlin/JS webApp..."
  ./gradlew :webApp:jsBrowserDevelopmentWebpack --no-daemon --quiet
  echo "Build complete."
fi

fuser -k 5000/tcp 2>/dev/null || true
fuser -k 31997/tcp 2>/dev/null || true
sleep 1

echo "Starting web server..."
node serve.js
