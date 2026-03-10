# StartDrive — Driving School App

A Kotlin Multiplatform project with:
- **Android app** (Kotlin, Jetpack Compose, Material 3)
- **Web frontend** (Kotlin/JS compiled to JavaScript via Gradle/webpack)

## Stack

- **Build system:** Gradle (Kotlin DSL), Kotlin Multiplatform
- **Frontend:** Kotlin/JS compiled with webpack, served by Node.js on port 5000
- **Backend/DB:** Firebase (Auth, Firestore, Realtime Database, Storage, Cloud Messaging)
- **Java runtime:** GraalVM 22.3 (Java 19)

## Project Layout

- `webApp/` — Kotlin/JS web frontend source
  - `src/jsMain/kotlin/` — Kotlin source files
  - `src/jsMain/resources/` — Static assets (index.html, CSS, images, firebase-config.js)
  - `build/kotlin-webpack/js/developmentExecutable/` — webpack output (webApp.js)
  - `build/processedResources/js/main/` — processed static resources
- `app/` — Android app (Kotlin, Jetpack Compose)
- `shared/` — Shared Kotlin Multiplatform code
- `functions/` — Firebase Cloud Functions (Node.js)

## Running Locally

The workflow runs `bash start.sh` which:
1. Builds the Kotlin/JS app with `./gradlew :webApp:jsBrowserDevelopmentWebpack` if not already built
2. Serves static files at `http://0.0.0.0:5000` via `node serve.js`

## Firebase Configuration

Firebase config is in `webApp/src/jsMain/resources/firebase-config.js` and sets `window.__FIREBASE_CONFIG__`.
The project is connected to Firebase project `startdrive-573fa`.

## Build

To rebuild the web app after Kotlin source changes:
```bash
./gradlew :webApp:jsBrowserDevelopmentWebpack --no-daemon
```

Then restart the workflow.

## Android Build

```bash
./gradlew assembleDebug
```

Requires Android SDK (not available in Replit environment).
