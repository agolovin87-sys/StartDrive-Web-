# Скрипт: копирует SHA-1 в буфер обмена и открывает Firebase Console
$sha1 = "56:29:E8:0B:99:D1:D5:D6:53:9D:CF:52:99:E7:49:E0:0A:1D:7E:C6"
Set-Clipboard -Value $sha1
Start-Process "https://console.firebase.google.com/"
Write-Host "SHA-1 скопирован в буфер обмена. Открыта Firebase Console."
Write-Host "Вставь отпечаток (Ctrl+V) в Project settings -> Your apps -> Add fingerprint"
