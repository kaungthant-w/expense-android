# Test script for language switching functionality
Write-Host "Testing Language Switching Functionality" -ForegroundColor Green

# Clear previous logs
Write-Host "Clearing logs..." -ForegroundColor Yellow
adb logcat -c

# Start monitoring logs in background
Write-Host "Starting log monitoring..." -ForegroundColor Yellow
Start-Job -ScriptBlock {
    adb logcat -s MainActivity -s LanguageManager -s BaseActivity -s LanguageActivity 
} -Name "LogMonitor"

# Open the app
Write-Host "Opening MyApplication..." -ForegroundColor Yellow
adb shell monkey -p com.hsu.expense -c android.intent.category.LAUNCHER 1

Start-Sleep -Seconds 3

# Navigate to settings via navigation drawer 
Write-Host "Opening navigation drawer..." -ForegroundColor Yellow
adb shell input tap 50 100  # hamburger menu

Start-Sleep -Seconds 2

# Tap on Settings
Write-Host "Tapping Settings..." -ForegroundColor Yellow
adb shell input tap 200 800  # approximate settings position

Start-Sleep -Seconds 2

# Tap on Language Settings
Write-Host "Tapping Language Settings..." -ForegroundColor Yellow
adb shell input tap 200 300  # approximate language settings position

Start-Sleep -Seconds 2

# Tap on language spinner
Write-Host "Opening language spinner..." -ForegroundColor Yellow
adb shell input tap 200 400  # approximate spinner position

Start-Sleep -Seconds 1

# Select a different language (Myanmar)
Write-Host "Selecting Myanmar language..." -ForegroundColor Yellow
adb shell input tap 200 500  # approximate Myanmar option

Start-Sleep -Seconds 3

# Go back to main screen to observe changes
Write-Host "Going back to main screen..." -ForegroundColor Yellow
adb shell input keyevent KEYCODE_BACK
Start-Sleep -Seconds 1
adb shell input keyevent KEYCODE_BACK

Start-Sleep -Seconds 2

# Get the logs
Write-Host "Getting logs..." -ForegroundColor Yellow
Stop-Job -Name "LogMonitor"
$logOutput = Receive-Job -Name "LogMonitor"
Remove-Job -Name "LogMonitor"

Write-Host "=== LOG OUTPUT ===" -ForegroundColor Cyan
$logOutput | Select-Object -Last 20

Write-Host "Language switching test completed!" -ForegroundColor Green
