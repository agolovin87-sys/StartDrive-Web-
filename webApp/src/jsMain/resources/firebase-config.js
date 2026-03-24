/**
 * Конфиг Firebase для веб-версии StartDrive (из Firebase Console → Project settings → Your apps → Web).
 */
window.__FIREBASE_CONFIG__ = {
  apiKey: "AIzaSyADZiLy7HaTedftl9e_4is-3TsmlnFA82Y",
  authDomain: "startdrive-573fa.firebaseapp.com",
  databaseURL: "https://startdrive-573fa-default-rtdb.europe-west1.firebasedatabase.app",
  projectId: "startdrive-573fa",
  storageBucket: "startdrive-573fa.firebasestorage.app",
  messagingSenderId: "73391012476",
  appId: "1:73391012476:web:775bd2274627d25b9fa7f9",
  measurementId: "G-7VK3J524XJ"
};

// Web Push VAPID key (Firebase Console -> Cloud Messaging -> Web Push certificates).
// Без него браузер не выдаст FCM-токен для web push.
window.__FIREBASE_WEB_PUSH_VAPID_KEY__ = "BNOVfsGVLH8tPoRMxhdGmGp1aTupfF5ynhnf9nlR-ECFjjQcBV3an8fYDwVHigl-Db7TqnRE9BHob-Uemkygmc0";
