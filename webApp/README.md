# StartDrive — веб-версия

Веб-приложение StartDrive (Kotlin/JS + Firebase). Функционал соответствует Android-приложению: вход, роли (админ, инструктор, курсант), главная, баланс, чат, запись на вождение, история, настройки, билеты ПДД.

## Сборка

Из корня проекта:

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

Или только сборка:

```bash
./gradlew :webApp:jsBrowserDevelopmentWebpack
```

Артефакты: `webApp/build/dist/js/productionRun/` (или `developmentRun`).

## Настройка Firebase

1. Создайте проект в [Firebase Console](https://console.firebase.google.com/).
2. Добавьте веб-приложение в проект и скопируйте конфиг (apiKey, authDomain, projectId, storageBucket, messagingSenderId, appId).
3. Задайте конфиг одним из способов:

   **Вариант A — в коде**  
   В `webApp/src/jsMain/kotlin/firebase/Firebase.kt` в функции `getFirebaseConfig()` подставьте свои значения в объект `json(...)` вместо `"YOUR_WEB_API_KEY"` и т.д.

   **Вариант B — в браузере**  
   Перед загрузкой приложения выполните в консоли:

   ```javascript
   window.__FIREBASE_CONFIG__ = {
     apiKey: "ваш-apiKey",
     authDomain: "ваш-проект.firebaseapp.com",
     projectId: "ваш-projectId",
     storageBucket: "ваш-проект.firebasestorage.app",
     messagingSenderId: "число",
     appId: "ваш-appId"
   };
   ```

4. **Firestore**: включите Firestore в проекте. Правила доступа должны разрешать чтение/запись коллекций `users`, `driving_sessions`, `instructor_open_windows`, подколлекции `balance_history` и т.д. — по аналогии с правилами для Android-клиента.
5. **Realtime Database**: используется для чата (коллекция `chats` / сообщения). Включите Realtime Database и настройте правила.

## Структура

- `src/jsMain/kotlin/` — точка входа `Main.kt`, `AppState.kt`, рендер панелей и вкладок.
- `src/jsMain/kotlin/firebase/` — инициализация Firebase, Auth, Firestore, Realtime DB; функции для пользователей, чата, вождения, баланса.
- `src/jsMain/resources/style.css` — стили.
- Общая модель и пути — в модуле `shared`.

## Роли

- **Администратор**: главная (заявки, инструкторы, курсанты; активация, назначение курсанта, удаление), баланс (выбор пользователя, зачислить/списать/установить), чат, история (баланс, вождение, чат).
- **Инструктор**: главная (баланс, окна, занятия), запись (добавление окон, удаление), чат, билеты, история, настройки.
- **Курсант**: главная (баланс, инструктор, занятия), запись (слоты, мои записи), чат, билеты, история, настройки.
