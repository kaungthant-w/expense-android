# Test script to verify back button functionality in all secondary activities
Write-Host "=== Testing Back Button Implementation ===" -ForegroundColor Green

# Start the app
Write-Host "`n1. Starting the application..." -ForegroundColor Yellow
adb shell am start -n com.hsu.expense/.MainActivity

Start-Sleep 3

# Test Summary Activity back button
Write-Host "`n2. Testing Summary Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening Summary Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 400  # Click Summary
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test Analytics Activity back button
Write-Host "`n3. Testing Analytics Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening Analytics Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 450  # Click Analytics
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test All List Activity back button
Write-Host "`n4. Testing All List Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening All List Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 500  # Click All List
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test History Activity back button
Write-Host "`n5. Testing History Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening History Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 550  # Click History
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test Currency Exchange Activity back button
Write-Host "`n6. Testing Currency Exchange Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening Currency Exchange Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 600  # Click Currency Exchange
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test Settings Activity back button
Write-Host "`n7. Testing Settings Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening Settings Activity via navigation drawer"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 650  # Click Settings
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

# Test Feedback Activity back button (via Settings)
Write-Host "`n8. Testing Feedback Activity back button..." -ForegroundColor Yellow
Write-Host "   - Opening Settings Activity"
adb shell input tap 50 100  # Open drawer
Start-Sleep 1
adb shell input tap 300 650  # Click Settings
Start-Sleep 2
Write-Host "   - Opening Feedback Activity from Settings"
adb shell input tap 540 600  # Click Feedback card
Start-Sleep 2
Write-Host "   - Clicking back button"
adb shell input tap 50 150  # Click back button
Start-Sleep 2

Write-Host "`n=== Back Button Testing Complete ===" -ForegroundColor Green
Write-Host "All secondary activities should now have functional back buttons!" -ForegroundColor Cyan
Write-Host "Manually verify that each back button returns to the previous screen." -ForegroundColor Yellow
