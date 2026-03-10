# Конфиг Firebase для веб-версии

Чтобы работали вход и регистрация, нужно подставить конфиг веб-приложения Firebase.

## Где взять конфиг

1. Открой [Firebase Console](https://console.firebase.google.com/) → проект StartDrive.
2. Project settings (шестерёнка) → раздел **Your apps**.
3. Если веб-приложения ещё нет: нажми **Add app** → **Web** (</>).
4. Скопируй объект `firebaseConfig` (apiKey, authDomain, projectId, storageBucket, messagingSenderId, appId).

## Как задать конфиг

**Вариант 1 — через консоль браузера (для проверки)**  
Перед входом открой консоль (F12) и выполни:

```javascript
window.__FIREBASE_CONFIG__ = {
  apiKey: "твой-apiKey",
  authDomain: "startdrive-573fa.firebaseapp.com",
  projectId: "startdrive-573fa",
  storageBucket: "startdrive-573fa.firebasestorage.app",
  messagingSenderId: "твой-senderId",
  appId: "твой-appId"
};
```

Затем обнови страницу.

**Вариант 2 — через скрипт в index.html**  
В `webApp/src/jsMain/resources/index.html` перед `<script src="webApp.js">` добавь:

```html
<script>
  window.__FIREBASE_CONFIG__ = {
    apiKey: "твой-apiKey",
    authDomain: "startdrive-573fa.firebaseapp.com",
    projectId: "startdrive-573fa",
    storageBucket: "startdrive-573fa.firebasestorage.app",
    messagingSenderId: "твой-senderId",
    appId: "твой-appId"
  };
</script>
```

Подставь свои значения из Firebase Console. После этого собери проект заново.

## Домен в Firebase

В Firebase Console → Authentication → Settings → Authorized domains добавь:
- `localhost` (уже есть по умолчанию для разработки);
- домен, на котором будет открываться веб-версия (например, `твой-сайт.web.app`).
