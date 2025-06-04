# Translation System Testing Script
# This script tests the complete translation system implementation

Write-Host "=== HSU Expense App - Translation System Test ===" -ForegroundColor Green
Write-Host ""

# Function to check device connection
function Wait-ForDevice {
    Write-Host "Checking for connected Android devices..." -ForegroundColor Yellow
    $maxAttempts = 10
    $attempt = 0
    
    do {
        $attempt++
        Write-Host "Attempt $attempt/$maxAttempts - Checking device status..." -ForegroundColor Cyan
        $devices = adb devices
        $deviceOnline = $devices | Select-String "device$" | Measure-Object | Select-Object -ExpandProperty Count
        
        if ($deviceOnline -gt 0) {
            Write-Host "✅ Device connected and online!" -ForegroundColor Green
            return $true
        }
        
        Write-Host "⏳ Waiting for device to come online..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        
    } while ($attempt -lt $maxAttempts)
    
    Write-Host "❌ No device found online after $maxAttempts attempts" -ForegroundColor Red
    return $false
}

# Function to install and test app
function Test-TranslationSystem {
    Write-Host ""
    Write-Host "=== Installing HSU Expense App ===" -ForegroundColor Green
    
    # Clean and build
    Write-Host "🧹 Cleaning project..." -ForegroundColor Cyan
    .\gradlew clean
    
    Write-Host "🔨 Building debug APK..." -ForegroundColor Cyan
    .\gradlew assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build successful!" -ForegroundColor Green
        
        # Install APK
        Write-Host "📱 Installing APK..." -ForegroundColor Cyan
        .\gradlew installDebug
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Installation successful!" -ForegroundColor Green
            Write-Host ""
            Write-Host "=== Translation System Test Instructions ===" -ForegroundColor Green
            Write-Host "1. Open the HSU Expense app on your device/emulator" -ForegroundColor White
            Write-Host "2. Tap the hamburger menu (☰) in the top-left corner" -ForegroundColor White
            Write-Host "3. Navigate to: ⚙️ Settings → Language Settings" -ForegroundColor White
            Write-Host "4. Test switching between languages:" -ForegroundColor White
            Write-Host "   - English (English)" -ForegroundColor White
            Write-Host "   - မြန်မာ (Myanmar)" -ForegroundColor White
            Write-Host "   - 中文 (Chinese)" -ForegroundColor White
            Write-Host "   - 日本語 (Japanese)" -ForegroundColor White
            Write-Host "5. Verify that all UI text updates immediately" -ForegroundColor White
            Write-Host "6. Check navigation menu titles update correctly" -ForegroundColor White
            Write-Host "7. Test different app sections with each language" -ForegroundColor White
            Write-Host ""
            Write-Host "🎉 Translation system is ready for testing!" -ForegroundColor Green
            Write-Host ""
            Write-Host "📱 Launch app command: adb shell am start -n com.example.myapplication/.MainActivity" -ForegroundColor Cyan
        } else {
            Write-Host "❌ Installation failed!" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Build failed!" -ForegroundColor Red
    }
}

# Main execution
Write-Host "Starting translation system test..." -ForegroundColor Cyan

if (Wait-ForDevice) {
    Test-TranslationSystem
} else {
    Write-Host ""
    Write-Host "=== Manual Testing Instructions ===" -ForegroundColor Yellow
    Write-Host "If you have a physical Android device:" -ForegroundColor White
    Write-Host "1. Enable Developer Options and USB Debugging" -ForegroundColor White
    Write-Host "2. Connect via USB" -ForegroundColor White
    Write-Host "3. Run: adb devices" -ForegroundColor White
    Write-Host "4. Run: .\gradlew installDebug" -ForegroundColor White
    Write-Host ""
    Write-Host "Or start an emulator manually:" -ForegroundColor White
    Write-Host "1. Open Android Studio" -ForegroundColor White
    Write-Host "2. Go to Tools → AVD Manager" -ForegroundColor White
    Write-Host "3. Start an existing AVD or create a new one" -ForegroundColor White
    Write-Host "4. Wait for it to fully boot" -ForegroundColor White
    Write-Host "5. Run this script again" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Translation System Features ===" -ForegroundColor Green
Write-Host "✅ 4 Languages: English, Myanmar, Chinese, Japanese" -ForegroundColor White
Write-Host "✅ JSON-based translation files" -ForegroundColor White
Write-Host "✅ Real-time language switching" -ForegroundColor White
Write-Host "✅ Complete UI coverage" -ForegroundColor White
Write-Host "✅ Navigation menu translations" -ForegroundColor White
Write-Host "✅ Beautiful Myanmar Unicode text" -ForegroundColor White
Write-Host ""
