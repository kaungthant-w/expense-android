#!/usr/bin/env powershell

Write-Host "=== Comprehensive Language Switching Test ===" -ForegroundColor Green

# Clear logcat and start monitoring
Write-Host "Clearing logs and starting fresh monitoring..." -ForegroundColor Yellow
adb logcat -c
Start-Sleep -Seconds 1

# Start logcat monitoring in background
$logcatJob = Start-Job -ScriptBlock {
    adb logcat -s "LanguageManager" "BaseActivity" -v time
}

Write-Host "Starting the app for comprehensive language test..." -ForegroundColor Yellow
adb shell monkey -p com.hsu.expense -c android.intent.category.LAUNCHER 1
Start-Sleep -Seconds 3

Write-Host "`n=== COMPREHENSIVE TEST STEPS ===" -ForegroundColor Cyan
Write-Host "Please perform the following tests in order:" -ForegroundColor White

Write-Host "`n1. BASIC LANGUAGE SWITCHING:" -ForegroundColor Yellow
Write-Host "   - Open navigation drawer" -ForegroundColor White
Write-Host "   - Go to Settings > Language Settings" -ForegroundColor White
Write-Host "   - Change: English -> Myanmar" -ForegroundColor White
Write-Host "   - Verify: Language Activity text updates immediately" -ForegroundColor White
Write-Host "   - Press Apply or Back to return to Settings" -ForegroundColor White
Write-Host "   - Verify: Settings page is now in Myanmar" -ForegroundColor White

Write-Host "`n2. NAVIGATION DRAWER TEST:" -ForegroundColor Yellow
Write-Host "   - Open navigation drawer" -ForegroundColor White
Write-Host "   - Verify: All menu items are in Myanmar" -ForegroundColor White
Write-Host "   - Go to Home (MainActivity)" -ForegroundColor White
Write-Host "   - Verify: Main page content is in Myanmar" -ForegroundColor White

Write-Host "`n3. MULTIPLE LANGUAGE CHANGES:" -ForegroundColor Yellow
Write-Host "   - Go back to Language Settings" -ForegroundColor White
Write-Host "   - Change: Myanmar -> Chinese" -ForegroundColor White
Write-Host "   - Verify: Immediate change in Language Activity" -ForegroundColor White
Write-Host "   - Change: Chinese -> Japanese" -ForegroundColor White
Write-Host "   - Verify: Immediate change in Language Activity" -ForegroundColor White
Write-Host "   - Change: Japanese -> English" -ForegroundColor White
Write-Host "   - Verify: Back to English" -ForegroundColor White

Write-Host "`n4. CROSS-ACTIVITY TEST:" -ForegroundColor Yellow
Write-Host "   - Navigate to different sections:" -ForegroundColor White
Write-Host "     * All List" -ForegroundColor White
Write-Host "     * History" -ForegroundColor White
Write-Host "     * Summary" -ForegroundColor White
Write-Host "     * Currency Exchange" -ForegroundColor White
Write-Host "   - Verify: All activities show content in selected language" -ForegroundColor White

Write-Host "`n=== EXPECTED RESULTS ===" -ForegroundColor Green
Write-Host "✓ Language changes IMMEDIATELY when dropdown selection changes" -ForegroundColor White
Write-Host "✓ No app restart required" -ForegroundColor White
Write-Host "✓ Navigation drawer updates instantly" -ForegroundColor White
Write-Host "✓ All activities respect the new language" -ForegroundColor White
Write-Host "✓ Settings and other pages update without recreation" -ForegroundColor White

Write-Host "`nPress any key when all testing is complete..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Stop the logcat job and show results
Write-Host "`nStopping log monitoring..." -ForegroundColor Yellow
Stop-Job $logcatJob
$logOutput = Receive-Job $logcatJob
Remove-Job $logcatJob

Write-Host "`n=== LANGUAGE SWITCHING LOGS ===" -ForegroundColor Cyan
if ($logOutput) {
    $logOutput | ForEach-Object { Write-Host $_ -ForegroundColor Gray }
} else {
    Write-Host "No specific logs captured. Check logcat manually if needed." -ForegroundColor Yellow
}

Write-Host "`n=== TEST COMPLETED ===" -ForegroundColor Green
Write-Host "Language switching functionality has been tested." -ForegroundColor White
Write-Host "If you observed immediate language changes without app restart, the fix is successful!" -ForegroundColor Green
