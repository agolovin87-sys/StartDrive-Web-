# Индексы Firestore для StartDrive

Если в логах приложения появляется ошибка **«The query requires an index»** для коллекций `driving_sessions` или `instructor_open_windows`, нужно задеплоить составные индексы.

## Быстрый способ (одна команда)

В корне проекта выполните:

```bash
firebase deploy --only firestore:indexes
```

Индексы из файла `firestore.indexes.json` будут отправлены в проект. В консоли Firebase (раздел **Firestore** → **Indexes**) статус сменится на **Building**, через 1–5 минут — на **Enabled**. После этого запросы на вкладке «Запись» инструктора начнут работать без ошибки.

## Если не используете Firebase CLI

1. Откройте [Firebase Console](https://console.firebase.google.com) → проект **startdrive-573fa**.
2. Перейдите в **Firestore** → **Indexes** (вкладка «Индексы»).
3. Нажмите **Create index** (Создать индекс).
4. Создайте два составных индекса:

   **Индекс 1 — сессии по инструктору:**
   - Collection ID: `driving_sessions`
   - Поля: `instructorId` (Ascending), `startTime` (Ascending)

   **Индекс 2 — окна по инструктору:**
   - Collection ID: `instructor_open_windows`
   - Поля: `instructorId` (Ascending), `dateTime` (Ascending)

5. Сохраните и дождитесь статуса **Enabled**.

## Про ошибку «Target id not found: 14»

Сообщение `WatchStream: Target id not found: 14` связано с внутренним состоянием Firestore и обычно исчезает после переподключения или повторного открытия экрана. После создания индексов запросы перестанут падать с **FAILED_PRECONDITION**, и такие предупреждения тоже должны сократиться.
