#!/usr/bin/env pwsh
# Enhanced Language Switching Test Script
# This script tests the immediate language switching functionality for:
# - Navigation Drawer
# - Today's Summary Card  
# - TabLayout for Data Lists
# - All other UI elements

Write-Host "=== Enhanced Language Switching Test ===" -ForegroundColor Green
Write-Host "Testing immediate language switching without app restart" -ForegroundColor Yellow

# Check if device is connected
$devices = adb devices
if ($devices -notmatch "device") {
    Write-Host "❌ No Android device connected. Please connect a device and try again." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Android device connected" -ForegroundColor Green

# Check if app is installed
$packages = adb shell pm list packages | Select-String "com.hsu.expense"
if (-not $packages) {
    Write-Host "❌ App not installed. Installing now..." -ForegroundColor Yellow
    adb install -r "app/build/outputs/apk/debug/app-debug.apk"
}

Write-Host "✅ App is installed" -ForegroundColor Green

# Start the app
Write-Host "`n🚀 Starting the app..." -ForegroundColor Cyan
adb shell am start -n "com.hsu.expense/.SplashActivity"
Start-Sleep -Seconds 3

Write-Host "`n📋 LANGUAGE SWITCHING TEST PLAN:" -ForegroundColor Cyan
Write-Host "1. We will switch to Myanmar language" -ForegroundColor White
Write-Host "2. Check Navigation Drawer titles" -ForegroundColor White  
Write-Host "3. Check Today's Summary Card" -ForegroundColor White
Write-Host "4. Check TabLayout titles" -ForegroundColor White
Write-Host "5. Verify ALL elements update immediately" -ForegroundColor White

Write-Host "`n⚠️  MANUAL TESTING REQUIRED:" -ForegroundColor Yellow
Write-Host "Please follow these steps on your device:" -ForegroundColor White

Write-Host "`n📍 STEP 1: Navigate to Language Settings" -ForegroundColor Cyan
Write-Host "   • Tap the hamburger menu (≡) in the top-left" -ForegroundColor White
Write-Host "   • Tap 'Settings' in the navigation drawer" -ForegroundColor White
Write-Host "   • Tap 'Language Settings' card" -ForegroundColor White

Write-Host "`n📍 STEP 2: Change Language to Myanmar" -ForegroundColor Cyan
Write-Host "   • In the language dropdown, select 'မြန်မာ (Myanmar)'" -ForegroundColor White
Write-Host "   • Tap 'Apply' button" -ForegroundColor White

Write-Host "`n🔍 STEP 3: Immediate Verification (WITHOUT restarting app)" -ForegroundColor Cyan
Write-Host "   • Go back to main screen" -ForegroundColor White
Write-Host "   • Open navigation drawer (≡) and check menu items are in Myanmar:" -ForegroundColor White
Write-Host "     ✅ Should see: '🏠 မူလစာမျက်နှာ' instead of 'Home'" -ForegroundColor Green
Write-Host "     ✅ Should see: '📋 စာရင်းအားလုံး' instead of 'All List'" -ForegroundColor Green
Write-Host "     ✅ Should see: '🗃️ မှတ်တမ်း' instead of 'History'" -ForegroundColor Green

Write-Host "`n   • Check Today's Summary Card:" -ForegroundColor White
Write-Host "     ✅ Title should show: '📅 ယနေ့ စာရင်းချုပ်' instead of 'Today's Summary'" -ForegroundColor Green
Write-Host "     ✅ Labels should be in Myanmar" -ForegroundColor Green

Write-Host "`n   • Check TabLayout at the bottom:" -ForegroundColor White
Write-Host "     ✅ Should see: 'အားလုံး' instead of 'All'" -ForegroundColor Green
Write-Host "     ✅ Should see: 'ယနေ့' instead of 'Today'" -ForegroundColor Green
Write-Host "     ✅ Should see: 'ဒီအပတ်' instead of 'This Week'" -ForegroundColor Green
Write-Host "     ✅ Should see: 'ဒီလ' instead of 'This Month'" -ForegroundColor Green

Write-Host "`n📍 STEP 4: Test Input Form" -ForegroundColor Cyan
Write-Host "   • Tap the '+' button to add expense" -ForegroundColor White
Write-Host "   • Check modal dialog is in Myanmar" -ForegroundColor White
Write-Host "   • Cancel the dialog" -ForegroundColor White

Write-Host "`n📍 STEP 5: Test Other Languages" -ForegroundColor Cyan
Write-Host "   • Repeat the process with Chinese (中文) and Japanese (日本語)" -ForegroundColor White
Write-Host "   • Verify all UI elements update immediately" -ForegroundColor White

Write-Host "`n🎯 EXPECTED RESULTS:" -ForegroundColor Magenta
Write-Host "✅ Navigation Drawer should update immediately" -ForegroundColor Green
Write-Host "✅ Today's Summary Card should update immediately" -ForegroundColor Green  
Write-Host "✅ TabLayout should update immediately" -ForegroundColor Green
Write-Host "✅ All text should change without app restart" -ForegroundColor Green
Write-Host "✅ No English text should remain visible" -ForegroundColor Green

Write-Host "❌ FAILURE INDICATORS:" -ForegroundColor Red
Write-Host "❌ Navigation menu still shows English after language change" -ForegroundColor Red
Write-Host "❌ Today's Summary Card still shows English" -ForegroundColor Red
Write-Host "❌ Tab titles still show English" -ForegroundColor Red
Write-Host "❌ Need to restart app to see changes" -ForegroundColor Red

Write-Host "`n🔧 ENHANCED FEATURES BEING TESTED:" -ForegroundColor Blue
Write-Host "• Enhanced onLanguageChanged() method with proper update order" -ForegroundColor White
Write-Host "• updateNavigationMenuTitles() with forced invalidation" -ForegroundColor White
Write-Host "• updateTodaySummaryCard() with forced refresh" -ForegroundColor White
Write-Host "• updateTabTitles() with forced layout refresh" -ForegroundColor White
Write-Host "• runOnUiThread() calls for immediate UI updates" -ForegroundColor White

Write-Host "`nPress Enter when you have completed the manual testing..." -ForegroundColor Yellow
Read-Host

# Test navigation by switching between activities
Write-Host "`n🔄 Testing Navigation Between Activities..." -ForegroundColor Cyan
Write-Host "Testing navigation to verify language persists across activities:" -ForegroundColor White

# Navigate to All List
Write-Host "`n📋 Opening All List..." -ForegroundColor Yellow
adb shell input tap 50 50  # Menu button
Start-Sleep -Seconds 1
adb shell input tap 300 400  # All List item (approximate)
Start-Sleep -Seconds 2

# Navigate to History  
Write-Host "`n🗃️ Opening History..." -ForegroundColor Yellow
adb shell input tap 50 50  # Menu button
Start-Sleep -Seconds 1
adb shell input tap 300 500  # History item (approximate)
Start-Sleep -Seconds 2

# Back to main
adb shell input keyevent KEYCODE_BACK
Start-Sleep -Seconds 1

Write-Host "`n🎉 Enhanced Language Switching Test Complete!" -ForegroundColor Green
Write-Host "`nSUMMARY OF IMPROVEMENTS:" -ForegroundColor Cyan
Write-Host "✨ Enhanced MainActivity.onLanguageChanged() with proper update order" -ForegroundColor Green
Write-Host "✨ Added forced UI invalidation and layout refresh" -ForegroundColor Green
Write-Host "✨ Added runOnUiThread() calls for immediate updates" -ForegroundColor Green
Write-Host "✨ Enhanced logging for debugging language change process" -ForegroundColor Green
Write-Host "✨ Fixed compilation errors and missing FAB references" -ForegroundColor Green

Write-Host "`nPlease report results:" -ForegroundColor Yellow
Write-Host "✅ SUCCESS: All UI elements update immediately without restart" -ForegroundColor Green
Write-Host "❌ PARTIAL: Some elements update, others require restart" -ForegroundColor Yellow
Write-Host "❌ FAILURE: No immediate updates, restart still required" -ForegroundColor Red
