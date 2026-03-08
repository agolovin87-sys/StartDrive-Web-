# StartDrive — приложение для автошколы

Мобильное приложение на Android (Kotlin, Jetpack Compose) с тремя ролями: **Администратор**, **Инструктор**, **Курсант**.

## Стек

- **UI:** Jetpack Compose, Material 3
- **База данных:** Firebase (Auth, Firestore, Realtime Database, Storage, Cloud Messaging)

## Настройка Firebase

1. Создайте проект в [Firebase Console](https://console.firebase.google.com).
2. Добавьте Android-приложение с package name `com.example.startdrive`.
3. Скачайте `google-services.json` и замените им файл `app/google-services.json` (сейчас в проекте — заглушка).
4. Включите в консоли:
   - **Authentication** (Email/Password)
   - **Firestore Database**
   - **Realtime Database**
   - **Storage** (по желанию, для аватарок)
   - **Cloud Messaging**
5. Разверните индексы Firestore: в консоли Firestore → Indexes → добавить индексы из `firestore.indexes.json` или выполнить:
   ```bash
   firebase deploy --only firestore:indexes
   ```
6. Правила Firestore и Realtime Database настройте под вашу модель доступа (см. документацию Firebase).

## Роль администратора

Роль `admin` по умолчанию не доступна при регистрации. Первого администратора нужно создать вручную:

- Зарегистрируйте пользователя через приложение (любая роль).
- В консоли Firestore откройте коллекцию `users`, найдите документ пользователя и измените поле `role` на `admin`, а `isActive` на `true`.

## Разрешения

- **POST_NOTIFICATIONS** — запрашивается при необходимости для Android 13+.
- **CALL_PHONE** — запрашивается при первом нажатии «Позвонить».

## Сборка

```bash
./gradlew assembleDebug
```

Или откройте проект в Android Studio и запустите на устройстве/эмуляторе.

## Структура БД (кратко)

- **Firestore**
  - `users` — пользователи (поля: fullName, email, phone, role, balance, fcmToken, assignedInstructorId / assignedCadets, isActive, createdAt).
  - `users/{userId}/balance_history` — история операций по балансу.
  - `driving_sessions` — сессии вождения (instructorId, cadetId, startTime, status, рейтинги и т.д.).
  - `instructor_open_windows` — свободные/забронированные окна инструктора.

- **Realtime Database**
  - `chats/{chatRoomId}/messages` — сообщения чата (senderId, text, timestamp, status).
  - `presence/{userId}` — статус онлайн (status, lastSeen).

## Уведомления (FCM)

Токен устройства сохраняется в поле `fcmToken` документа пользователя в Firestore при входе. Отправку уведомлений (новое сообщение, напоминание за 15 минут, изменение баланса и т.д.) удобно реализовать через **Cloud Functions** и при необходимости **Cloud Scheduler**.
