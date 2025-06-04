#!/usr/bin/env powershell

Write-Host "=== COMPREHENSIVE LANGWrite-Host "EXPECTED BEHAVIOR:" -ForegroundColor Green
Write-Host "- Dropdown shows all 4 languages correctly" -ForegroundColor Green
Write-Host "- Toast appears with translated message" -ForegroundColor Green
Write-Host "- Navigation drawer immediately shows translated menu items" -ForegroundColor Green
Write-Host "- Main screen titles, buttons, tabs update immediately" -ForegroundColor Green
Write-Host "- Language persists after app restart" -ForegroundColor GreenWITCHING TEST ===" -ForegroundColor Green
Write-Host "Testing Japanese and Chinese language switching functionality..." -ForegroundColor Yellow

Write-Host "`n📱 STEP 1: Test Current Language Detection" -ForegroundColor Cyan
Write-Host "1. Open the HSU Expense app (should be running now)" -ForegroundColor White
Write-Host "2. Check current language displayed in UI" -ForegroundColor White
Write-Host "3. Note down what language is currently active" -ForegroundColor White

Write-Host "`n📱 STEP 2: Navigate to Language Settings" -ForegroundColor Cyan
Write-Host "1. Tap the hamburger menu (☰) in the top-left corner" -ForegroundColor White
Write-Host "2. Tap 'Settings' in the navigation drawer" -ForegroundColor White
Write-Host "3. Tap 'Language Settings' card" -ForegroundColor White

Write-Host "`n📱 STEP 3: Test Japanese Language Switching" -ForegroundColor Cyan
Write-Host "1. In the language dropdown, select '日本語 (Japanese)'" -ForegroundColor White
Write-Host "2. Check if dropdown shows Japanese correctly" -ForegroundColor White
Write-Host "3. Check if the Apply button text changes" -ForegroundColor White
Write-Host "4. Tap 'Apply' button" -ForegroundColor White
Write-Host "5. Look for Toast notification message" -ForegroundColor White
Write-Host "6. Go back to main screen (use back button)" -ForegroundColor White
Write-Host "7. Check navigation drawer menu items" -ForegroundColor White
Write-Host "8. Check if main screen elements are in Japanese" -ForegroundColor White

Write-Host "`n📱 STEP 4: Test Chinese Language Switching" -ForegroundColor Cyan
Write-Host "1. Go back to Settings → Language Settings" -ForegroundColor White
Write-Host "2. In the language dropdown, select '中文 (Chinese)'" -ForegroundColor White
Write-Host "3. Check if dropdown shows Chinese correctly" -ForegroundColor White
Write-Host "4. Check if the Apply button text changes" -ForegroundColor White
Write-Host "5. Tap 'Apply' button" -ForegroundColor White
Write-Host "6. Look for Toast notification message" -ForegroundColor White
Write-Host "7. Go back to main screen (use back button)" -ForegroundColor White
Write-Host "8. Check navigation drawer menu items" -ForegroundColor White
Write-Host "9. Check if main screen elements are in Chinese" -ForegroundColor White

Write-Host "`n📱 STEP 5: Test Language Persistence" -ForegroundColor Cyan
Write-Host "1. Close the app completely (recent apps → swipe up)" -ForegroundColor White
Write-Host "2. Reopen the app" -ForegroundColor White
Write-Host "3. Check if the last selected language persists" -ForegroundColor White

Write-Host "`n📱 STEP 6: Test All Language Options" -ForegroundColor Cyan
Write-Host "1. Go to Language Settings again" -ForegroundColor White
Write-Host "2. Test each language option in this order:" -ForegroundColor White
Write-Host "   a. English" -ForegroundColor Gray
Write-Host "   b. မြန်မာ (Myanmar)" -ForegroundColor Gray
Write-Host "   c. 中文 (Chinese)" -ForegroundColor Gray
Write-Host "   d. 日本語 (Japanese)" -ForegroundColor Gray
Write-Host "3. For each language, check:" -ForegroundColor White
Write-Host "   - Dropdown selection works" -ForegroundColor Gray
Write-Host "   - Toast message appears" -ForegroundColor Gray
Write-Host "   - Navigation drawer updates" -ForegroundColor Gray
Write-Host "   - Main screen updates" -ForegroundColor Gray

Write-Host "WHAT TO LOOK FOR (POTENTIAL ISSUES):" -ForegroundColor Magenta
Write-Host "- Dropdown doesn't show Japanese/Chinese text correctly" -ForegroundColor Red
Write-Host "- Toast notification doesn't appear" -ForegroundColor Red
Write-Host "- Toast message appears in wrong language" -ForegroundColor Red
Write-Host "- Navigation drawer items don't change language" -ForegroundColor Red
Write-Host "- Main screen elements remain in English" -ForegroundColor Red
Write-Host "- App crashes when selecting Japanese/Chinese" -ForegroundColor Red
Write-Host "- No visual feedback when language is selected" -ForegroundColor Red

Write-Host "`n✅ EXPECTED BEHAVIOR:" -ForegroundColor Green
Write-Host "- Dropdown shows all 4 languages correctly" -ForegroundColor Green
Write-Host "✓ Toast appears: '言語が変更されました' (Japanese) or '语言已更改' (Chinese)" -ForegroundColor Green
Write-Host "✓ Navigation drawer immediately shows translated menu items" -ForegroundColor Green
Write-Host "✓ Main screen titles, buttons, tabs update immediately" -ForegroundColor Green
Write-Host "✓ Language persists after app restart" -ForegroundColor Green

Write-Host "`n📋 DEBUGGING INFORMATION TO COLLECT:" -ForegroundColor Yellow
Write-Host "1. Screenshot of language dropdown showing all options" -ForegroundColor White
Write-Host "2. Screenshot of main screen in Japanese" -ForegroundColor White
Write-Host "3. Screenshot of main screen in Chinese" -ForegroundColor White
Write-Host "4. Note any error messages or crashes" -ForegroundColor White
Write-Host "5. Check if log monitoring shows language change events" -ForegroundColor White

Write-Host "`n🚀 AUTOMATED LOG MONITORING STARTED" -ForegroundColor Cyan
Write-Host "Log monitoring is running in background..." -ForegroundColor White
Write-Host "Look for these log entries during testing:" -ForegroundColor White
Write-Host "• LanguageManager: Setting language from X to Y" -ForegroundColor Gray
Write-Host "• LanguageManager: Broadcasting language change" -ForegroundColor Gray
Write-Host "• MainActivity: Received language change broadcast" -ForegroundColor Gray

Write-Host "`n📱 START TESTING NOW!" -ForegroundColor Green -BackgroundColor Black
Write-Host "Follow the steps above and report any issues you encounter." -ForegroundColor White
Write-Host "Press Ctrl+C when done to stop log monitoring." -ForegroundColor Yellow

Write-Host "Press any key to continue with testing..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
