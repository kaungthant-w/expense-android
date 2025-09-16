# Test script for FAB and Language Switching functionality
# FAB and Language Switching Test Script

Write-Host "=== FAB and Language Switching Functionality Test ===" -ForegroundColor Green

Write-Host "`n1. Checking if MyApplication is installed..." -ForegroundColor Yellow
$installed = adb shell pm list packages | Select-String "com.hsu.expense"
if ($installed) {
    Write-Host "✓ MyApplication is installed" -ForegroundColor Green
} else {
    Write-Host "✗ MyApplication is not installed" -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Monitoring logs for MainActivity activity..." -ForegroundColor Yellow
Write-Host "Please manually open the MyApplication app on your device and:" -ForegroundColor Cyan
Write-Host "  a) Test FAB button - click the FAB to see if modal dialog opens" -ForegroundColor Cyan
Write-Host "  b) Test Language Switching - go to Settings and change language" -ForegroundColor Cyan
Write-Host "  c) Check if Navigation Drawer, Summary, and TabLayout update language" -ForegroundColor Cyan
Write-Host "`nWatching logs for 30 seconds..." -ForegroundColor Yellow

# Monitor logs for 30 seconds
adb logcat -s MainActivity:D -T 5 &
Start-Sleep 30

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "Please verify:" -ForegroundColor Yellow
Write-Host "1. FAB button shows the add expense modal dialog" -ForegroundColor White
Write-Host "2. Language switching updates all UI elements immediately" -ForegroundColor White
Write-Host "3. Navigation menu titles change language" -ForegroundColor White
Write-Host "4. Summary card updates language" -ForegroundColor White
Write-Host "5. Tab titles update language" -ForegroundColor White
