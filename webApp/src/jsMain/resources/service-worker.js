const CACHE_NAME = "startdrive-cache-v2";
const APP_SHELL = [
  "/",
  "/index.html",
  "/manifest.webmanifest",
  "/style.css",
  "/webApp.js",
  "/firebase-config.js",
  "/app-icon.png"
];

// FCM background notifications (compat SDK in service worker).
try {
  importScripts("https://www.gstatic.com/firebasejs/10.12.5/firebase-app-compat.js");
  importScripts("https://www.gstatic.com/firebasejs/10.12.5/firebase-messaging-compat.js");
  firebase.initializeApp({
    apiKey: "AIzaSyADZiLy7HaTedftl9e_4is-3TsmlnFA82Y",
    authDomain: "startdrive-573fa.firebaseapp.com",
    projectId: "startdrive-573fa",
    storageBucket: "startdrive-573fa.firebasestorage.app",
    messagingSenderId: "73391012476",
    appId: "1:73391012476:web:775bd2274627d25b9fa7f9"
  });
  const messaging = firebase.messaging();
  messaging.onBackgroundMessage((payload) => {
    const title =
      (payload && payload.notification && payload.notification.title) || "StartDrive";
    const body =
      (payload && payload.notification && payload.notification.body) || "";
    const icon = "/app-icon.png";
    self.registration.showNotification(title, {
      body,
      icon,
      badge: icon,
      data: payload && payload.data ? payload.data : {}
    });
  });
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
  const isNavigation = req.mode === "navigate";

  if (isNavigation) {
    event.respondWith(
      fetch(req).catch(() => caches.match("/index.html"))
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
