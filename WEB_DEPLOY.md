# Сборка и деплой веб-приложения StartDrive

## Production-сборка

Из корня проекта:

```bash
.\gradlew.bat :webApp:jsBrowserProductionWebpack
```

Артефакты появятся в:

```
webApp/build/dist/js/productionExecutable/
```

В этой папке будут:
- **index.html** — главная страница
- **webApp.js** — минифицированный бандл
- **style.css** — стили (если подключаются из `jsMain/resources`)

## Деплой на Firebase Hosting

1. Установи [Firebase CLI](https://firebase.google.com/docs/cli) и выполни `firebase login`.
2. В корне проекта в **firebase.json** добавь (если ещё нет):

```json
"hosting": {
  "public": "webApp/build/dist/js/productionExecutable",
  "ignore": ["firebase.json", "**/.*", "**/node_modules/**"]
}
```

3. Собери веб и разверни:

```bash
.\gradlew.bat :webApp:jsBrowserProductionWebpack
firebase deploy --only hosting
```

После деплоя в консоли будет ссылка вида `https://<project>.web.app`.

## Деплой на другой хостинг

Скопируй целиком содержимое папки `webApp/build/dist/js/productionExecutable/` на любой статический хостинг (GitHub Pages, Netlify, Vercel, свой сервер). Корень сайта должен отдавать **index.html**, а в нём подключены **webApp.js** и **style.css**.

## Разработка (dev-сервер)

```bash
.\gradlew.bat :webApp:jsBrowserDevelopmentRun
```

С авто-пересборкой при изменениях:

```bash
.\gradlew.bat :webApp:jsBrowserDevelopmentRun --continuous
```

Откроется http://localhost:8080/.
