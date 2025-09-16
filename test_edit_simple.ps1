# AllListActivity Edit Functionality Test
Write-Host "Testing AllListActivity Edit Functionality" -ForegroundColor Cyan
Write-Host "Starting HSU Expense App..." -ForegroundColor Green

adb shell am start -n com.hsu.expense/.MainActivity

Write-Host ""
Write-Host "Test Steps:" -ForegroundColor Yellow
Write-Host "1. Navigate to All Expenses from navigation drawer"
Write-Host "2. Single-tap any expense item"
Write-Host "3. Verify ExpenseDetailActivity opens"
Write-Host "4. Edit fields and save"
Write-Host "5. Check changes are visible"
Write-Host ""
Write-Host "Expected: Single tap opens editing, long press enters selection mode"

Read-Host "Press Enter when testing complete"
Write-Host "Testing Complete!" -ForegroundColor Green
