# Настройка Firebase для StartDrive

Краткий чеклист. Подробности — в **FIREBASE_ADD_THIS.txt**.

## 1. Отпечатки (SHA) для Android

- Открой [Firebase Console](https://console.firebase.google.com/) → проект → Project settings → Your apps.
- Для приложения `com.example.startdrive` нажми **Add fingerprint**.
- Вставь **SHA-1** (см. FIREBASE_ADD_THIS.txt).
- При необходимости добавь SHA-256.
- Скачай обновлённый **google-services.json** и положи в `app/google-services.json`.
- Пересобери приложение.

Если нужен актуальный SHA-1:
```bash
.\gradlew signingReport
```

## 2. Storage (аватарки и голосовые)

- В консоли: **Build → Storage → вкладка Rules**.
- Либо вставь содержимое файла **storage.rules** из корня проекта и нажми **Опубликовать**.
- Либо разверни через CLI: `firebase deploy --only storage`.

Правила уже настроены в репозитории: `users/{userId}/` (аватар), `chats/voice/{chatRoomId}/` (голосовые).

## 3. Firestore (база данных)

- В консоли: **Firestore Database → Rules**.
- Используй правила из **firestore.rules** или образец из FIREBASE_ADD_THIS.txt (без путей к файлам вроде chat_avatar.png — они только в Storage).

## 4. Что уже есть в проекте

- В **app**: плагин `google-services`, зависимости Firebase (Auth, Firestore, Database, Storage, Messaging).
- В корне: **firebase.json** (Firestore, Storage, Functions), **storage.rules**, **firestore.rules**.

После выполнения шагов 1–3 приложение должно корректно работать с Auth, Firestore, Storage и уведомлениями.
