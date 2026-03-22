# Настройка групповых чатов (Firestore) — 3 шага

Если видите **«Missing or insufficient permissions»**, пройдите все пункты по порядку.

## 1. Опубликовать правила Firestore

Из корня репозитория:

```bash
firebase deploy --only firestore:rules
```

Или вручную: [Firebase Console](https://console.firebase.google.com/project/startdrive-573fa/firestore/rules) → **Firestore Database** → **Правила** → вставить содержимое файла `firestore.rules` → **Опубликовать**.

Проект по умолчанию: **startdrive-573fa** (см. `.firebaserc`).

---

## 2. Роль администратора в коллекции `users`

Правила для `chat_groups` и `cadet_groups` проверяют: в документе **`users/{ваш UID}`** поле **`role`** должно быть строкой **`admin`**.

1. Откройте [Firestore](https://console.firebase.google.com/project/startdrive-573fa/firestore/data/~2Fusers).
2. Найдите документ с ID = **UID** пользователя из **Authentication** (тот же, под кем входите в веб-приложение).
3. Убедитесь, что есть поле **`role`** = **`admin`** (латиница, без пробелов).

Если документа нет — пользователь не сможет пройти проверку `exists()` в правилах; создайте документ при регистрации или вручную по образцу других пользователей.

---

## 3. Исправить или удалить «битые» документы `chat_groups`

У каждого документа в коллекции **`chat_groups`** поле **`memberIds`** должно быть **массивом строк** (array of strings), в нём обязательно должен быть UID администратора, создавшего группу.

### Вручную (Console)

1. [Коллекция `chat_groups`](https://console.firebase.google.com/project/startdrive-573fa/firestore/data/~2Fchat_groups)
2. Откройте документ: тип **`memberIds`** — **array**.
3. Если там map/объект или пусто — **удалите документ** и создайте группу заново из приложения (после п.1–2).

### Скрипт (рекомендуется при многих документах)

См. `functions/scripts/cleanup-invalid-chat-groups.js` и раздел «Запуск» ниже.

---

## Запуск скрипта очистки

Требуется [ключ сервисного аккаунта](https://console.firebase.google.com/project/startdrive-573fa/settings/serviceaccounts/adminsdk) (JSON). Файл **не коммитьте** (см. `.gitignore`).

**PowerShell (Windows):**

```powershell
cd functions
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\путь\к\вашему-service-account.json"
node scripts/cleanup-invalid-chat-groups.js
```

**bash:**

```bash
cd functions
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
node scripts/cleanup-invalid-chat-groups.js
```

Скрипт удаляет только документы, где `memberIds` отсутствует, не массив или пустой.
