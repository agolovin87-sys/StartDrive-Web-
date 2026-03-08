# Ошибка DEVELOPER_ERROR / Unknown calling package (Google Play Services)

Если в логах видите:
- `SecurityException: Unknown calling package name 'com.google.android.gms'`
- `ConnectionResult{statusCode=DEVELOPER_ERROR}`

это значит, что в Firebase/Google Cloud не добавлен отпечаток (SHA-1) ключа, которым подписывается приложение.

## Что сделать

### 1. Узнать SHA-1 отладочного ключа

В корне проекта выполните:

**Windows (PowerShell):**
```powershell
cd android
.\gradlew signingReport
```

Или вручную (путь к ключу по умолчанию для debug):
```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**macOS/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Скопируйте строки **SHA1:** и при желании **SHA-256:**.

### 2. Добавить отпечатки в Firebase

1. Откройте [Firebase Console](https://console.firebase.google.com/).
2. Выберите проект приложения StartDrive.
3. Откройте **Project settings** (иконка шестерёнки) → вкладка **General**.
4. В блоке **Your apps** найдите приложение с пакетом `com.example.startdrive`.
5. Нажмите **Add fingerprint**, вставьте SHA-1, сохраните. При желании добавьте и SHA-256.
6. Скачайте обновлённый **google-services.json** и положите его в `app/google-services.json` (замените текущий файл).

### 3. Проверить OAuth (если используете вход через Google)

Если в приложении есть вход через Google (Google Sign-In):

1. Откройте [Google Cloud Console](https://console.cloud.google.com/) → тот же проект.
2. **APIs & Services** → **Credentials**.
3. В блоке **OAuth 2.0 Client IDs** откройте клиент типа **Android** (или создайте его).
4. Убедитесь, что указаны:
   - **Package name:** `com.example.startdrive`
   - **SHA-1 certificate fingerprint:** тот же SHA-1, что добавлен в Firebase.

После добавления SHA-1 и обновления `google-services.json` пересоберите приложение и установите заново. Ошибка DEVELOPER_ERROR и «Unknown calling package» должны исчезнуть.
