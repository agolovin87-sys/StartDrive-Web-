# Настройка CORS для загрузки аватара (подробно)

Без CORS на бакете Firebase Storage при загрузке аватара с веб-приложения появляется ошибка **«Нет соединения с интернетом»**. Настройку нужно выполнить **один раз**. Ниже — пошаговая инструкция.

---

## Способ 1: Скрипт с ключом сервисного аккаунта (рекомендуется)

Не требует установки Google Cloud SDK. Нужны только Node.js и ключ из Firebase.

---

### Шаг 1. Открыть настройки проекта в Firebase

1. Откройте в браузере:  
   **https://console.firebase.google.com/project/startdrive-573fa/settings/serviceaccounts/adminsdk**

2. Если попросит войти — войдите в аккаунт Google, с которым создан проект Firebase.

3. Вы должны оказаться на странице **«Учётные записи служб»** (Service accounts) в настройках проекта **startdrive-573fa**.

   Если открылась другая страница:
   - Нажмите на **шестерёнку** рядом с названием проекта → **Настройки проекта** (Project settings).
   - Перейдите на вкладку **«Учётные записи служб»** (Service accounts).

---

### Шаг 2. Создать и скачать закрытый ключ

1. На странице «Учётные записи служб» найдите блок **«Firebase Admin SDK»**.

2. В нём нажмите кнопку **«Создать закрытый ключ»** (или **«Generate new private key»**).

3. Появится предупреждение, что ключ даёт полный доступ к проекту. Нажмите **«Создать ключ»** (или **«Generate key»**).

4. В папку «Загрузки» (Downloads) скачается файл в формате JSON, имя обычно такое:  
   `startdrive-573fa-firebase-adminsdk-xxxxx-xxxxxxxxxx.json`  
   Запомните, куда он сохранился (часто `C:\Users\ВашеИмя\Downloads`).

**Важно:** этот файл — секретный. Не выкладывайте его в интернет и не добавляйте в репозиторий. В проекте он уже добавлен в `.gitignore`.

---

### Шаг 3. Положить ключ в папку `functions` и переименовать

1. Откройте **Проводник** (Win+E).

2. Перейдите в папку проекта:  
   **`c:\StartDrive\functions`**  
   В ней должны быть файлы `package.json`, папка `node_modules`, папка `scripts` и т.д.

3. Откройте папку **«Загрузки»** (Downloads) и найдите скачанный JSON-файл ключа.

4. **Скопируйте** этот файл (Ctrl+C) и **вставьте** его в папку `c:\StartDrive\functions` (Ctrl+V).

5. **Переименуйте** скопированный файл в точное имя:  
   **`service-account-key.json`**  
   (без лишних пробелов, расширение должно быть `.json`).

6. В итоге в папке `c:\StartDrive\functions` должен лежать файл:  
   **`c:\StartDrive\functions\service-account-key.json`**

Проверка: откройте папку `functions` и убедитесь, что там есть файл с именем `service-account-key.json`.

---

### Шаг 4. Открыть терминал в папке проекта

1. Откройте **терминал** в Cursor/VS Code: меню **Terminal** → **New Terminal** (или Ctrl+`).

2. Убедитесь, что вы в корне проекта. Если нет — перейдите в папку `functions`:
   ```powershell
   cd c:\StartDrive\functions
   ```

3. Должна отображаться строка вида:
   ```text
   PS C:\StartDrive\functions>
   ```
   или
   ```text
   C:\StartDrive\functions>
   ```

---

### Шаг 5. Запустить установку CORS

В терминале (из папки `functions`) выполните **один** из вариантов:

- **Через npm** (рекомендуется):
  ```powershell
  cd c:\StartDrive\functions
  npm run set-cors
  ```

- **Или напрямую:**
  ```powershell
  cd c:\StartDrive\functions
  node scripts/set-storage-cors.js
  ```

- **Или двойной щелчок** по файлу `functions\run-set-cors.cmd` в Проводнике.

2. Подождите 2–5 секунд.

3. **Успех** — в консоли появится строка:
   ```text
   CORS для Storage установлен.
   ```
   После этого загрузка аватара с веба должна работать. Можно проверить в приложении: Настройки → выбрать фото → Готово.

4. **Ошибка «Could not load the default credentials»** — скрипт не нашёл ключ. Проверьте:
   - Файл лежит именно в `c:\StartDrive\functions\service-account-key.json`.
   - Имя файла точно `service-account-key.json` (без опечаток).
   - Вы запускаете команду из папки `c:\StartDrive\functions` (команда `cd c:\StartDrive\functions` уже была выполнена).

5. **Ошибка «ENOENT: no such file»** — путь к файлу неверный. Укажите путь к ключу явно (подставьте свой путь к файлу):
   ```powershell
   $env:GOOGLE_APPLICATION_CREDENTIALS="c:\StartDrive\functions\service-account-key.json"
   node scripts/set-storage-cors.js
   ```

6. **Другая ошибка** — скопируйте текст ошибки и проверьте, что JSON-файл не повреждён и скачан из правильного проекта Firebase.

---

### Шаг 6. (Необязательно) Удалить ключ с диска

После успешной настройки CORS ключ для скрипта больше не нужен. Для безопасности можно удалить файл `service-account-key.json` из папки `functions`. CORS останется включённым. При необходимости ключ можно снова скачать в Firebase Console и повторить шаги 3–5.

---

## Способ 2: Через gcloud (если уже установлен Google Cloud SDK)

1. Установите [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) и откройте терминал.

2. Выполните вход:
   ```bash
   gcloud auth application-default login
   ```
   Откроется браузер для входа в аккаунт Google.

3. Перейдите в папку проекта и запустите скрипт:
   ```powershell
   cd c:\StartDrive\functions
   node scripts/set-storage-cors.js
   ```

---

## Способ 3: Только команда gcloud (без скрипта)

Если установлен Google Cloud SDK и вы авторизованы (`gcloud auth login`):

```bash
cd c:\StartDrive
gcloud storage buckets update gs://startdrive-573fa.appspot.com --cors-file=storage.cors.json
```

Файл `storage.cors.json` лежит в корне проекта (`c:\StartDrive\storage.cors.json`).

---

## Что такое CORS и зачем это нужно

Веб-приложение открывается с одного адреса (например, `http://localhost:8080` или ваш домен), а загрузка файлов идёт на другой — серверы Firebase Storage. Браузер по умолчанию блокирует такие «кросс-доменные» запросы. CORS — это набор заголовков, которые разрешают браузеру выполнять запросы к Storage с вашего сайта. Без настройки CORS на бакете загрузка аватара в браузере не сработает и вы увидите ошибку «Нет соединения с интернетом».

После выполнения одного из способов выше CORS будет настроен один раз и загрузка аватара с веба будет работать.
