# Set Storage CORS for avatar upload. Put service-account-key.json in this folder first.
$keyPath = Join-Path $PSScriptRoot "service-account-key.json"
if (-not (Test-Path $keyPath)) {
    Write-Host "File not found: $keyPath" -ForegroundColor Red
    Write-Host "Download key from Firebase Console, save as service-account-key.json here." -ForegroundColor Yellow
    exit 1
}
$env:GOOGLE_APPLICATION_CREDENTIALS = $keyPath
Set-Location $PSScriptRoot
node scripts/set-storage-cors.js
if ($LASTEXITCODE -eq 0) {
    Write-Host "Done. Avatar upload should work now." -ForegroundColor Green
}
