# Test script for feedback functionality
Write-Host "Testing Feedback Functionality..." -ForegroundColor Green

# Launch the app
Write-Host "1. Launching MainActivity..." -ForegroundColor Yellow
adb shell am start -n com.hsu.expense/.MainActivity

Start-Sleep -Seconds 3

# Simulate clicking the FAB to open menu
Write-Host "2. Opening FAB menu..." -ForegroundColor Yellow
adb shell input tap 350 600  # Approximate FAB position

Start-Sleep -Seconds 1

# Simulate clicking feedback option
Write-Host "3. Clicking Feedback option..." -ForegroundColor Yellow
adb shell input tap 300 500  # Approximate feedback button position

Start-Sleep -Seconds 2

# Check if FeedbackActivity launched successfully
Write-Host "4. Checking current activity..." -ForegroundColor Yellow
adb shell "dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'"

Write-Host "Test completed. Check logcat for any errors." -ForegroundColor Green
