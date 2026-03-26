const CACHE_NAME = "startdrive-cache-v4";
const APP_SHELL = [
  "/",
  "/index.html",
  "/manifest.webmanifest",
  "/style.css",
  "/webApp.js",
  "/firebase-config.js",
  "/pdd-tickets-bundle.js",
  "/app-icon.png"
];

/** Сборка и стили: сначала сеть (с revalidate), иначе после деплоя SW отдавал старый webApp.js из кэша. */
const NETWORK_FIRST_PATHS = new Set([
  "/webApp.js",
  "/style.css",
  "/firebase-config.js",
  "/pdd-tickets-bundle.js",
  "/index.html"
]);

// FCM в SW: только инициализация. Показ — из webpush в Cloud Functions (без общего notification + webpush).
try {
  importScripts("https://www.gstatic.com/firebasejs/10.14.0/firebase-app-compat.js");
  importScripts("https://www.gstatic.com/firebasejs/10.14.0/firebase-messaging-compat.js");
  firebase.initializeApp({
    apiKey: "AIzaSyADZiLy7HaTedftl9e_4is-3TsmlnFA82Y",
    authDomain: "startdrive-573fa.firebaseapp.com",
    projectId: "startdrive-573fa",
    storageBucket: "startdrive-573fa.firebasestorage.app",
    messagingSenderId: "73391012476",
    appId: "1:73391012476:web:775bd2274627d25b9fa7f9"
  });
  firebase.messaging();
} catch (_) {}

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  const req = event.request;
  const path = new URL(req.url).pathname;
  const isNavigation = req.mode === "navigate";

  if (isNavigation) {
    event.respondWith(
      fetch(req, { cache: "no-cache" }).catch(() => caches.match("/index.html"))
    );
    return;
  }

  if (NETWORK_FIRST_PATHS.has(path)) {
    event.respondWith(
      fetch(req, { cache: "no-cache" })
        .then((networkRes) => {
          if (networkRes && networkRes.ok) {
            const copy = networkRes.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(req, copy)).catch(() => {});
          }
          return networkRes;
        })
        .catch(() => caches.match(req))
    );
    return;
  }

  event.respondWith(
    caches.match(req).then((cached) => {
      if (cached) return cached;
      return fetch(req)
        .then((networkRes) => {
          const copy = networkRes.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(req, copy)).catch(() => {});
          return networkRes;
        })
        .catch(() => cached);
    })
  );
});
