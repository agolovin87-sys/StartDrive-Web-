# Cloud Functions — уведомления о новых сообщениях

При появлении нового сообщения в Realtime Database функция отправляет push-уведомление (FCM) получателю со звуком.

## Развёртывание

1. Установите Firebase CLI: `npm install -g firebase-tools`
2. Войдите: `firebase login`
3. В корне проекта: `firebase init functions` — выберите существующую папку `functions`, язык Node, ESLint по желанию.
4. Перейдите в папку: `cd functions`
5. Установите зависимости: `npm install`
6. Разверните: `firebase deploy --only functions`

**Важно:** Нужен тариф Blaze (по себестоимости). Регион в `index.js` (`europe-west1`) должен совпадать с регионом вашей Realtime Database (проверьте в Firebase Console).

## Проверка

После деплоя при отправке сообщения в чат получатель получит push с заголовком «Новое сообщение» и текстом сообщения (со звуком).
