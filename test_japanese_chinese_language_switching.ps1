#!/usr/bin/env powershell

Write-Host "=== JAPANESE & CHINESE LANGUAGE SWITCHING TEST ===" -ForegroundColor Green
Write-Host "Testing specifically Japanese and Chinese language switching functionality" -ForegroundColor Yellow
Write-Host ""

# Function to test specific language switching
function Test-LanguageSwitching {
    param($language, $languageName)
    
    Write-Host "`n--- Testing $languageName Language ---" -ForegroundColor Cyan
    
    # Clear logcat and start monitoring
    Write-Host "Clearing device logs..." -ForegroundColor Gray
    adb logcat -c
    
    # Start logcat monitoring in background
    Write-Host "Starting log monitoring for language changes..." -ForegroundColor Gray
    $logJob = Start-Job -ScriptBlock {
        param($lang)
        adb logcat | Select-String "LanguageManager.*$lang|Broadcasting.*$lang|onLanguageChanged"
    } -ArgumentList $language
    
    Write-Host "✅ Log monitoring started. Job ID: $($logJob.Id)" -ForegroundColor Green
    
    return $logJob
}

# Test Japanese language switching
Write-Host "📱 MANUAL TESTING INSTRUCTIONS:" -ForegroundColor Magenta
Write-Host ""

Write-Host "🇯🇵 STEP 1: Test Japanese Language" -ForegroundColor Yellow
Write-Host "1. Open the HSU Expense app on your device" -ForegroundColor White
Write-Host "2. Navigate: ☰ Menu → Settings → Language Settings" -ForegroundColor White
Write-Host "3. In the dropdown, select: 日本語 (Japanese)" -ForegroundColor White
Write-Host "4. Observe if the language changes IMMEDIATELY" -ForegroundColor White
Write-Host "5. Check if interface text updates to Japanese characters" -ForegroundColor White
Write-Host ""

Write-Host "Expected Japanese translations:" -ForegroundColor Gray
Write-Host "• Language Settings → 言語設定" -ForegroundColor Gray
Write-Host "• Apply → 適用" -ForegroundColor Gray
Write-Host "• Settings → 設定" -ForegroundColor Gray
Write-Host ""

Write-Host "🇨🇳 STEP 2: Test Chinese Language" -ForegroundColor Yellow
Write-Host "1. In the same Language Settings page" -ForegroundColor White
Write-Host "2. Change dropdown to: 中文 (Chinese)" -ForegroundColor White
Write-Host "3. Observe if the language changes IMMEDIATELY" -ForegroundColor White
Write-Host "4. Check if interface text updates to Chinese characters" -ForegroundColor White
Write-Host ""

Write-Host "Expected Chinese translations:" -ForegroundColor Gray
Write-Host "• Language Settings → 语言设置" -ForegroundColor Gray
Write-Host "• Apply → 应用" -ForegroundColor Gray
Write-Host "• Settings → 设置" -ForegroundColor Gray
Write-Host ""

Write-Host "🔍 STEP 3: Verification Checklist" -ForegroundColor Yellow
Write-Host "For EACH language change, verify:" -ForegroundColor White
Write-Host "✓ Dropdown text updates immediately" -ForegroundColor Green
Write-Host "✓ Page title updates immediately" -ForegroundColor Green
Write-Host "✓ Apply button text updates immediately" -ForegroundColor Green
Write-Host "✓ Toast message appears in selected language" -ForegroundColor Green
Write-Host "✓ Navigation back shows updated navigation drawer" -ForegroundColor Green
Write-Host ""

Write-Host "❌ FAILURE INDICATORS:" -ForegroundColor Red
Write-Host "❌ Text remains in English after language selection" -ForegroundColor Red
Write-Host "❌ No toast message appears" -ForegroundColor Red
Write-Host "❌ App crashes or freezes" -ForegroundColor Red
Write-Host "❌ Language change requires app restart" -ForegroundColor Red
Write-Host ""

# Start monitoring for Japanese
Write-Host "🔧 DEBUGGING INFORMATION:" -ForegroundColor Magenta
Write-Host "Starting log monitoring for language switching events..." -ForegroundColor White

$jaJob = Test-LanguageSwitching "ja" "Japanese"
Start-Sleep 2
$zhJob = Test-LanguageSwitching "zh" "Chinese"

Write-Host ""
Write-Host "📋 LOG MONITORING ACTIVE" -ForegroundColor Cyan
Write-Host "Japanese monitoring: Job $($jaJob.Id)" -ForegroundColor Gray
Write-Host "Chinese monitoring: Job $($zhJob.Id)" -ForegroundColor Gray
Write-Host ""

Write-Host "🎯 TEST EXECUTION:" -ForegroundColor Magenta
Write-Host "Now perform the manual tests above..."
Write-Host "Press Enter when you've completed testing to check logs..."
Read-Host

# Check logs
Write-Host "`n📊 CHECKING LOGS:" -ForegroundColor Cyan

Write-Host "`nJapanese language logs:" -ForegroundColor Yellow
try {
    $jaLogs = Receive-Job $jaJob -Keep
    if ($jaLogs) {
        $jaLogs | ForEach-Object { Write-Host "JA LOG: $_" -ForegroundColor Green }
    } else {
        Write-Host "No Japanese language switching logs found" -ForegroundColor Red
    }
} catch {
    Write-Host "Error retrieving Japanese logs: $_" -ForegroundColor Red
}

Write-Host "`nChinese language logs:" -ForegroundColor Yellow
try {
    $zhLogs = Receive-Job $zhJob -Keep
    if ($zhLogs) {
        $zhLogs | ForEach-Object { Write-Host "ZH LOG: $_" -ForegroundColor Green }
    } else {
        Write-Host "No Chinese language switching logs found" -ForegroundColor Red
    }
} catch {
    Write-Host "Error retrieving Chinese logs: $_" -ForegroundColor Red
}

# Clean up jobs
Stop-Job $jaJob, $zhJob -ErrorAction SilentlyContinue
Remove-Job $jaJob, $zhJob -ErrorAction SilentlyContinue

Write-Host "`n🏁 TEST COMPLETED" -ForegroundColor Green
Write-Host "If you see logs above, language switching is working." -ForegroundColor White
Write-Host "If no logs appear, there may be an issue with the language switching system." -ForegroundColor Yellow

Write-Host "`n📝 NEXT STEPS:" -ForegroundColor Magenta
Write-Host "1. If Japanese/Chinese don't work but English/Myanmar do:" -ForegroundColor White
Write-Host "   → Check JSON file format in strings_ja.json and strings_zh.json" -ForegroundColor Gray
Write-Host "2. If no languages work:" -ForegroundColor White  
Write-Host "   → Check LanguageManager broadcasting system" -ForegroundColor Gray
Write-Host "3. If UI doesn't update:" -ForegroundColor White
Write-Host "   → Check onLanguageChanged() implementations" -ForegroundColor Gray

Write-Host "`nTest completed. Check your device for language switching behavior." -ForegroundColor Cyan
