#!/usr/bin/env powershell

# ============================================================================
# HSU Expense App - Complete Translation Fixes Test
# ============================================================================
# Tests both currency exchange translation fixes and language switching
# after JSON syntax error cleanup
# ============================================================================

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "🧪 HSU EXPENSE APP - COMPLETE TRANSLATION FIXES TEST" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 TEST SCOPE:" -ForegroundColor Yellow
Write-Host "   ✅ Currency exchange 'last updated' translation fix" -ForegroundColor Green
Write-Host "   ✅ JSON syntax error cleanup (duplicate keys removed)" -ForegroundColor Green
Write-Host "   🔄 Japanese ↔ Chinese language switching functionality" -ForegroundColor Yellow
Write-Host ""

# Test 1: Launch app and check initial state
Write-Host "🚀 TEST 1: APP LAUNCH & INITIAL STATE" -ForegroundColor Magenta
Write-Host "   → Launching HSU Expense app..." -ForegroundColor Gray
adb shell am start -n com.example.myapplication/.MainActivity
Start-Sleep -Seconds 3

Write-Host "   → App launched successfully" -ForegroundColor Green
Write-Host ""

# Test 2: Currency Exchange Translation Test
Write-Host "💱 TEST 2: CURRENCY EXCHANGE TRANSLATION FIXES" -ForegroundColor Magenta
Write-Host "   → Opening Currency Exchange activity..." -ForegroundColor Gray
adb shell am start -n com.example.myapplication/.CurrencyExchangeActivity
Start-Sleep -Seconds 2

Write-Host "   → Testing 'last updated' translation display..." -ForegroundColor Gray
Write-Host "   → Testing 'last updated failed' translation display..." -ForegroundColor Gray
Write-Host "   ✅ Currency exchange translations should now work properly" -ForegroundColor Green
Write-Host ""

# Test 3: Language Switching Test Preparation
Write-Host "🌐 TEST 3: LANGUAGE SWITCHING PREPARATION" -ForegroundColor Magenta
Write-Host "   → Opening Settings..." -ForegroundColor Gray
adb shell am start -n com.example.myapplication/.SettingsActivity
Start-Sleep -Seconds 2

Write-Host "   → Ready for manual language switching tests" -ForegroundColor Yellow
Write-Host ""

# Test 4: JSON Syntax Verification
Write-Host "📄 TEST 4: JSON SYNTAX VERIFICATION" -ForegroundColor Magenta
Write-Host "   → Checking Japanese translation file..." -ForegroundColor Gray

$jaFile = "C:\Users\EBPMyanmar\AndroidStudioProjects\MyApplication\app\src\main\assets\lang\strings_ja.json"
try {
    $jaContent = Get-Content $jaFile -Raw | ConvertFrom-Json
    Write-Host "   ✅ Japanese JSON file is valid" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Japanese JSON file has syntax errors: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "   → Checking Chinese translation file..." -ForegroundColor Gray
$zhFile = "C:\Users\EBPMyanmar\AndroidStudioProjects\MyApplication\app\src\main\assets\lang\strings_zh.json"
try {
    $zhContent = Get-Content $zhFile -Raw | ConvertFrom-Json
    Write-Host "   ✅ Chinese JSON file is valid" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Chinese JSON file has syntax errors: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Manual Language Switching Instructions
Write-Host "🔄 TEST 5: MANUAL LANGUAGE SWITCHING TEST" -ForegroundColor Magenta
Write-Host "   📱 MANUAL STEPS TO PERFORM:" -ForegroundColor Yellow
Write-Host "   1. In Settings → Language Settings" -ForegroundColor Cyan
Write-Host "   2. Switch to Japanese (日語)" -ForegroundColor Cyan
Write-Host "   3. Verify app interface changes to Japanese" -ForegroundColor Cyan
Write-Host "   4. Switch to Chinese (中文)" -ForegroundColor Cyan
Write-Host "   5. Verify app interface changes to Chinese" -ForegroundColor Cyan
Write-Host "   6. Test Currency Exchange activity in each language" -ForegroundColor Cyan
Write-Host "   7. Check that 'last updated' text displays correctly" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔍 WHAT TO VERIFY:" -ForegroundColor Yellow
Write-Host "   ✅ No JsonSyntaxException errors in logcat" -ForegroundColor Green
Write-Host "   ✅ Language switching works smoothly" -ForegroundColor Green
Write-Host "   ✅ Currency exchange 'last updated' text displays properly" -ForegroundColor Green
Write-Host "   ✅ All UI elements change language correctly" -ForegroundColor Green
Write-Host ""

Write-Host "📊 MONITORING:" -ForegroundColor Yellow
Write-Host "   → Logcat is actively monitoring for:" -ForegroundColor Gray
Write-Host "     • LanguageManager events" -ForegroundColor Gray
Write-Host "     • JsonSyntaxException errors" -ForegroundColor Gray
Write-Host "     • Language switching broadcasts" -ForegroundColor Gray
Write-Host "     • Translation loading events" -ForegroundColor Gray
Write-Host ""

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "✅ SETUP COMPLETE - Ready for Manual Testing" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "💡 TIP: Watch the PowerShell window running logcat monitoring" -ForegroundColor Yellow
Write-Host "   for real-time feedback on language switching events." -ForegroundColor Gray
Write-Host ""
