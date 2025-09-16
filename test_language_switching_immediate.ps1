#!/usr/bin/env powershell

Write-Host "=== Testing Language Switching Functionality ===" -ForegroundColor Green

# Install and start the app
Write-Host "Installing and starting the app..." -ForegroundColor Yellow
adb install -r app\build\outputs\apk\debug\app-debug.apk
Start-Sleep -Seconds 2

# Start the app
adb shell monkey -p com.hsu.expense -c android.intent.category.LAUNCHER 1
Start-Sleep -Seconds 3

Write-Host "App started. Please follow these manual test steps:" -ForegroundColor Cyan
Write-Host "1. Open the navigation drawer (swipe from left or tap hamburger menu)" -ForegroundColor White
Write-Host "2. Navigate to Settings" -ForegroundColor White
Write-Host "3. Go to Language Settings" -ForegroundColor White
Write-Host "4. Change language from dropdown (try English -> Myanmar -> Chinese -> Japanese)" -ForegroundColor White
Write-Host "5. Check if navigation drawer and main page text changes immediately" -ForegroundColor White
Write-Host "6. Press back button to return to main screen" -ForegroundColor White
Write-Host "7. Verify all text is in the selected language" -ForegroundColor White

Write-Host "`nExpected behavior:" -ForegroundColor Green
Write-Host "- Language should change immediately when selected from dropdown" -ForegroundColor White
Write-Host "- Navigation drawer menu items should update instantly" -ForegroundColor White
Write-Host "- Main page text should update without app restart" -ForegroundColor White
Write-Host "- All other activities should reflect the new language" -ForegroundColor White

Write-Host "`nPress any key when testing is complete..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host "Test completed. Check the logs for any broadcast messages." -ForegroundColor Green
