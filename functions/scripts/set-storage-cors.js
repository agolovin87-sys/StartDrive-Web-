/**
 * Один раз запустить для настройки CORS на бакете Storage (загрузка аватара с веба).
 *
 * Вариант А — ключ сервисного аккаунта (без gcloud):
 *   1. Firebase Console → Настройки проекта → Учётные записи служб → Создать закрытый ключ.
 *   2. Сохранить JSON в functions/service-account-key.json (файл в .gitignore).
 *   3. В PowerShell: $env:GOOGLE_APPLICATION_CREDENTIALS="c:\StartDrive\functions\service-account-key.json"
 *   4. cd c:\StartDrive\functions && node scripts/set-storage-cors.js
 *
 * Вариант Б — gcloud: gcloud auth application-default login, затем node scripts/set-storage-cors.js
 */
const path = require("path");
const { initializeApp, cert } = require("firebase-admin/app");
const { getStorage } = require("firebase-admin/storage");

const keyPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || path.join(__dirname, "..", "service-account-key.json");
let appOptions = { storageBucket: "startdrive-573fa.firebasestorage.app" };
try {
  const fs = require("fs");
  const resolved = path.isAbsolute(keyPath) ? keyPath : path.resolve(__dirname, "..", keyPath);
  if (fs.existsSync(resolved)) {
    const key = JSON.parse(fs.readFileSync(resolved, "utf8"));
    appOptions.credential = cert(key);
  }
} catch (_) {}

initializeApp(appOptions);

const CORS = [
  {
    origin: ["*"],
    method: ["GET", "HEAD", "PUT", "POST", "OPTIONS", "DELETE"],
    responseHeader: ["Content-Type", "Content-Length", "x-goog-resumable", "x-goog-meta-*", "Authorization"],
    maxAgeSeconds: 7200,
  },
  {
    origin: [
      "http://localhost",
      "http://localhost:8080",
      "http://localhost:3000",
      "http://localhost:5000",
      "http://127.0.0.1",
      "http://127.0.0.1:8080",
      "http://127.0.0.1:3000",
      "https://localhost",
      "https://127.0.0.1",
      "https://startdrive-573fa.web.app",
      "https://startdrive-573fa.firebaseapp.com",
    ],
    method: ["GET", "HEAD", "PUT", "POST", "OPTIONS", "DELETE"],
    responseHeader: ["Content-Type", "Content-Length", "x-goog-resumable", "x-goog-meta-*", "Authorization"],
    maxAgeSeconds: 7200,
  },
];

async function main() {
  const storage = getStorage();
  const bucketNames = [
    "startdrive-573fa.firebasestorage.app",
    "startdrive-573fa.appspot.com",
  ];
  for (const name of bucketNames) {
    try {
      const bucket = storage.bucket(name);
      await bucket.setCorsConfiguration(CORS);
      console.log("CORS установлен для бакета:", name);
    } catch (err) {
      if (err.code === 404 || (err.message && err.message.includes("not found"))) {
        console.log("Бакет не найден (пропуск):", name);
      } else {
        console.error("Ошибка для бакета", name, err.message);
      }
    }
  }
  console.log("Готово.");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
