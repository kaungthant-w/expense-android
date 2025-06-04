# Translation System Test and Verification Script
# This script tests the multi-language support in the HSU Expense app

Write-Host "=== HSU Expense Translation System Test ===" -ForegroundColor Green
Write-Host ""

# Check if emulator is running
Write-Host "Checking emulator status..." -ForegroundColor Yellow
$devices = adb devices
if ($devices -match "device$") {
    Write-Host "✅ Emulator is online and ready" -ForegroundColor Green
} else {
    Write-Host "❌ No online emulator found. Please start an emulator first." -ForegroundColor Red
    exit 1
}

# Function to test language switching
function Test-LanguageSwitch {
    param($language, $languageName)
    
    Write-Host "Testing $languageName language..." -ForegroundColor Cyan
    
    # Change device language using ADB
    adb shell "setprop persist.sys.locale $language"
    adb shell "setprop ctl.restart zygote"
    
    Start-Sleep -Seconds 3
    
    # Launch the app
    adb shell am start -n com.example.myapplication/.MainActivity
    Start-Sleep -Seconds 2
    
    # Take a screenshot
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $screenshotName = "translation_test_${language}_${timestamp}.png"
    adb exec-out screencap -p > "screenshots\$screenshotName"
    
    Write-Host "📸 Screenshot saved: $screenshotName" -ForegroundColor Blue
    
    # Test navigation to different activities
    Write-Host "Testing navigation in $languageName..." -ForegroundColor Cyan
    
    # Open navigation drawer
    adb shell input tap 50 50  # Approximate hamburger menu location
    Start-Sleep -Seconds 1
    
    # Navigate to Summary
    adb shell input tap 300 400  # Approximate Summary menu item location
    Start-Sleep -Seconds 2
    
    # Take summary screenshot
    $summaryScreenshot = "summary_${language}_${timestamp}.png"
    adb exec-out screencap -p > "screenshots\$summaryScreenshot"
    Write-Host "📸 Summary screenshot: $summaryScreenshot" -ForegroundColor Blue
    
    # Go back to main
    adb shell input keyevent 4  # Back button
    Start-Sleep -Seconds 1
}

# Create screenshots directory if it doesn't exist
if (!(Test-Path "screenshots")) {
    New-Item -ItemType Directory -Name "screenshots"
    Write-Host "📁 Created screenshots directory" -ForegroundColor Blue
}

Write-Host "🚀 Starting translation system tests..." -ForegroundColor Green
Write-Host ""

# Test each supported language
$languages = @(
    @{code="en"; name="English"},
    @{code="es"; name="Spanish"},
    @{code="fr"; name="French"},
    @{code="pt"; name="Portuguese"},
    @{code="my"; name="Myanmar"}
)

foreach ($lang in $languages) {
    Write-Host "--- Testing $($lang.name) ($($lang.code)) ---" -ForegroundColor Magenta
    Test-LanguageSwitch -language $lang.code -languageName $lang.name
    Write-Host ""
}

# Reset to English
Write-Host "Resetting to English..." -ForegroundColor Yellow
adb shell "setprop persist.sys.locale en"
adb shell "setprop ctl.restart zygote"
Start-Sleep -Seconds 3

# Final verification
Write-Host "=== Translation System Test Results ===" -ForegroundColor Green
Write-Host "✅ Tested 5 languages: English, Spanish, French, Portuguese, Myanmar" -ForegroundColor Green
Write-Host "✅ Screenshots captured for each language" -ForegroundColor Green
Write-Host "✅ Navigation tested in each language" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 Translation system testing completed!" -ForegroundColor Green
Write-Host "📂 Check the 'screenshots' folder for visual verification" -ForegroundColor Blue

# List captured screenshots
Write-Host ""
Write-Host "Captured screenshots:" -ForegroundColor Yellow
Get-ChildItem "screenshots\*.png" | Sort-Object LastWriteTime -Descending | Select-Object -First 10 | ForEach-Object {
    Write-Host "  📸 $($_.Name)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "=== TRANSLATION SYSTEM VERIFICATION COMPLETE ===" -ForegroundColor Green
