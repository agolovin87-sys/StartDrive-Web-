# Content-Security-Policy (CSP) в веб-приложении StartDrive

## Где задаётся

- **В браузере:** тег `<meta http-equiv="Content-Security-Policy" ...>` в `src/jsMain/resources/index.html`.
- **На хостинге:** при деплое на Firebase Hosting CSP можно дополнительно задать через заголовки в `firebase.json` (секция `hosting.headers`), если нужна единая политика для всех страниц.

## Текущая политика (кратко)

| Директива    | Значение | Назначение |
|-------------|----------|------------|
| default-src | 'self'   | По умолчанию только свой origin |
| script-src  | 'self' 'unsafe-eval' https://*.firebasedatabase.app | Свои скрипты + Firebase Realtime Database (long-polling) |
| style-src   | 'self' 'unsafe-inline' | Свои стили + инлайн-стили в разметке |
| img-src     | 'self' data: https: | Картинки: свои, data-URI, Firebase Storage и др. |
| connect-src | 'self' + Firebase-домены | fetch/XHR/WebSocket: приложение и Firebase (Auth, Firestore, Realtime DB, Storage) |
| frame-src   | 'self' + firebaseapp.com, google.com | iframe для Firebase Auth и т.п. |

## Проверка

1. Открыть приложение, включить DevTools (F12) → вкладка **Console**.
2. При нарушении CSP браузер выведет сообщение вида:  
   `Refused to load ... because it violates the following Content-Security-Policy directive: ...`
3. По этому сообщению можно понять, какую директиву ослабить или какой источник добавить.

## Режим разработки

В dev (`jsBrowserDevelopmentRun`) источник — `http://localhost:8081`; для него `'self'` по-прежнему свой origin. Если появятся ошибки из-за eval или source maps, можно временно добавить в `script-src` значение `'unsafe-eval'` только для разработки (например, через отдельный index-dev.html или условный комментарий).

## Дополнительные заголовки на Firebase Hosting

Пример добавления CSP (и других заголовков) в `firebase.json`:

```json
"hosting": {
  "public": "webApp/build/kotlin-webpack/js/productionExecutable",
  "headers": [
    {
      "source": "**",
      "headers": [
        { "key": "Content-Security-Policy", "value": "default-src 'self'; ..." }
      ]
    }
  ]
}
```

Если CSP задаётся и в `<meta>`, и в заголовке ответа, действует **более строгий** вариант (пересечение правил).
