# Рабочий веб StartDrive

Краткая инструкция: как запустить и выложить обновлённый веб-интерфейс.

---

## Что нужно

- **JDK 11+** (например, из Android Studio: `C:\Program Files\Android\Android Studio\jbr`)
- **Сеть** — для входа и данных используется Firebase (конфиг в `webApp/src/jsMain/resources/firebase-config.js`)

---

## Запуск в режиме разработки (локально)

Самый простой способ — двойной клик по файлу в корне проекта:

```
run-web.bat
```

Скрипт выставит `JAVA_HOME` на JBR из Android Studio и запустит dev-сервер.

**Или из терминала (PowerShell):**

```powershell
cd c:\StartDrive
.\gradlew.bat :webApp:jsBrowserDevelopmentRun
```

После сборки откроется браузер на **http://localhost:8081/** (порт 8081, чтобы не конфликтовать с другими сервисами на 8080).

- Логин/регистрация — через Firebase Auth (Email/Password).
- Данные — Firestore и Realtime Database (проект из `firebase-config.js`).

---

## Production-сборка (для деплоя)

**Вариант 1 — один скрипт (рекомендуется):** двойной клик по `build-web.bat`.  
Скрипт соберёт бандл и скопирует в папку деплоя `index.html`, `style.css`, `firebase-config.js` и картинки.

**Вариант 2 — вручную:**

```powershell
.\gradlew.bat :webApp:jsBrowserProductionWebpack
# затем скопировать содержимое webApp/build/processedResources/js/main/ в webApp/build/kotlin-webpack/js/productionExecutable/
```

Готовая к выкладке папка:

```
webApp/build/kotlin-webpack/js/productionExecutable/
```

В ней: `index.html`, `webApp.js`, `style.css`, `firebase-config.js`, изображения. Эту папку можно отдавать любому статическому хостингу или деплоить на Firebase Hosting.

---

## Деплой на Firebase Hosting

В проекте уже настроен **firebase.json**: хостинг смотрит на папку production-сборки.

1. Установи [Firebase CLI](https://firebase.google.com/docs/cli) и выполни `firebase login`.
2. Собери веб и задеплой: запусти `build-web.bat` (или выполни `.\gradlew.bat :webApp:jsBrowserProductionWebpack` и скопируй ресурсы в `webApp/build/kotlin-webpack/js/productionExecutable/`), затем:

```powershell
firebase deploy --only hosting
```

В консоли будет ссылка вида `https://startdrive-573fa.web.app` (или твой проект).

---

## Проверка работоспособности

1. **Запуск** — `run-web.bat` или `.\gradlew.bat :webApp:jsBrowserDevelopmentRun`.
2. **Вход** — регистрация или логин (email/пароль). Первого админа создают вручную в Firestore (поле `role` = `admin`).
3. **Роли** — после входа доступны разделы: Админ (главная, баланс, чат, история), Инструктор (главная, запись, чат, **билеты ПДД**, история, настройки), Курсант (аналогично без админ-разделов).
4. **Билеты ПДД** — тесты загружаются из папки `pdd/` (билеты, знаки, разметка, штрафы). Источник при сборке: по умолчанию `Downloads/pdd_russia-master/pdd_russia-master` или `-PpddSource=путь`; иначе `app/src/main/assets/pdd`. В production папка `pdd` попадает из `processedResources` при копировании в `productionExecutable`.

Если страница не открывается или пустая — проверь консоль браузера (F12) и что в `firebase-config.js` указаны данные твоего проекта Firebase.

---

## Если не запускается

- **Порт 8081 занят** — закрой предыдущий запуск или освободи порт:
  ```powershell
  netstat -ano | findstr :8081
  taskkill /PID <номер_из_последней_колонки> /F
  ```
- **Java не найдена** — установи JDK 11+ или укажи путь в `run-web.bat` (JAVA_HOME). Если Java стоит в другом месте, задай переменную перед запуском или отредактируй скрипт.
- **Первый запуск долгий** — сборка и webpack могут занять 1–2 минуты; дождись сообщения «Project is running at: http://localhost:8081/».
- **Браузер не открылся** — открой вручную: http://localhost:8081/

---

## Структура веба (обновлённый контент)

| Путь | Назначение |
|------|------------|
| `webApp/src/jsMain/kotlin/Main.kt` | Точка входа, экраны, рендер (вход, панели, чат) |
| `webApp/src/jsMain/kotlin/AppState.kt` | Состояние приложения |
| `webApp/src/jsMain/kotlin/firebase/` | Firebase: Auth, Firestore, Realtime DB, чат, вождение, баланс |
| `webApp/src/jsMain/resources/style.css` | Стили (обновлённый дизайн: хедер, карточки, чат) |
| `webApp/src/jsMain/resources/index.html` | HTML-страница |
| `webApp/src/jsMain/resources/firebase-config.js` | Конфиг Firebase для веба |
| `webApp/src/jsMain/resources/pdd/` | Билеты ПДД, знаки, разметка, штрафы (копируются из pdd_russia-master или app assets) |
| `shared/` | Общие модели и пути (KMP) |

Изменения в дизайне (Replit) — в `style.css` и в разметке в `Main.kt` (карточки `.sd-ucard`, хедер с градиентом, логотип, форма входа).
