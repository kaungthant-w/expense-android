#!/usr/bin/env powershell

Write-Host "=== Testing History & All List Translation System ===" -ForegroundColor Cyan
Write-Host ""

# Function to test translation elements
function Test-TranslationElements {
    param([string]$Language, [string]$Title)
    
    Write-Host "Testing $Title Translation..." -ForegroundColor Yellow
    Write-Host "Please verify the following elements are translated:"
    Write-Host "✓ Page titles are translated" -ForegroundColor Green
    Write-Host "✓ Button texts are translated" -ForegroundColor Green  
    Write-Host "✓ Selection mode texts are translated" -ForegroundColor Green
    Write-Host "✓ Navigation menu items are translated" -ForegroundColor Green
    Write-Host ""
    
    $response = Read-Host "Are all elements properly translated? (y/n)"
    return $response -eq "y"
}

# Function to test language switching
function Test-LanguageSwitch {
    param([string]$Language)
    
    Write-Host "Switching to $Language..." -ForegroundColor Magenta
    Write-Host "Please:"
    Write-Host "1. Open Settings from navigation menu"
    Write-Host "2. Go to Language Settings"
    Write-Host "3. Select $Language"
    Write-Host "4. Navigate back to check translation"
    Write-Host ""
    
    Read-Host "Press Enter when ready to continue"
}

# Function to test page navigation and features
function Test-PageFeatures {
    param([string]$PageName)
    
    Write-Host "Testing $PageName Features..." -ForegroundColor Cyan
    Write-Host "Please test the following:"
    Write-Host "1. Navigate to $PageName"
    Write-Host "2. Check all text elements are translated"
    Write-Host "3. Test selection mode (if applicable)"
    Write-Host "4. Check button texts in different modes"
    Write-Host "5. Test navigation menu translation"
    Write-Host ""
    
    $response = Read-Host "Are all features working with proper translation? (y/n)"
    return $response -eq "y"
}

Write-Host "Starting translation test for History and All List pages..." -ForegroundColor Green
Write-Host ""

# Test English (default)
Write-Host "=== Testing English (Default) ===" -ForegroundColor White
$englishHistoryPassed = Test-PageFeatures "History Page"
$englishAllListPassed = Test-PageFeatures "All List Page"

# Test Myanmar
Test-LanguageSwitch "Myanmar (မြန်မာ)"
Write-Host "=== Testing Myanmar Translation ===" -ForegroundColor White
$myanmarHistoryPassed = Test-PageFeatures "History Page (Myanmar)"
$myanmarAllListPassed = Test-PageFeatures "All List Page (Myanmar)"

# Test Chinese
Test-LanguageSwitch "Chinese (中文)"
Write-Host "=== Testing Chinese Translation ===" -ForegroundColor White
$chineseHistoryPassed = Test-PageFeatures "History Page (Chinese)"
$chineseAllListPassed = Test-PageFeatures "All List Page (Chinese)"

# Test Japanese
Test-LanguageSwitch "Japanese (日本語)"
Write-Host "=== Testing Japanese Translation ===" -ForegroundColor White
$japaneseHistoryPassed = Test-PageFeatures "History Page (Japanese)"
$japaneseAllListPassed = Test-PageFeatures "All List Page (Japanese)"

# Switch back to English
Test-LanguageSwitch "English"

# Results Summary
Write-Host ""
Write-Host "=== TRANSLATION TEST RESULTS ===" -ForegroundColor Cyan
Write-Host ""

function Show-Result {
    param([bool]$Passed, [string]$TestName)
    $status = if ($Passed) { "✅ PASSED" } else { "❌ FAILED" }
    $color = if ($Passed) { "Green" } else { "Red" }
    Write-Host "$TestName`: $status" -ForegroundColor $color
}

Write-Host "History Page Translation:" -ForegroundColor Yellow
Show-Result $englishHistoryPassed "  English"
Show-Result $myanmarHistoryPassed "  Myanmar"
Show-Result $chineseHistoryPassed "  Chinese"
Show-Result $japaneseHistoryPassed "  Japanese"

Write-Host ""
Write-Host "All List Page Translation:" -ForegroundColor Yellow
Show-Result $englishAllListPassed "  English"
Show-Result $myanmarAllListPassed "  Myanmar"
Show-Result $chineseAllListPassed "  Chinese"
Show-Result $japaneseAllListPassed "  Japanese"

Write-Host ""

# Overall results
$allHistoryPassed = $englishHistoryPassed -and $myanmarHistoryPassed -and $chineseHistoryPassed -and $japaneseHistoryPassed
$allAllListPassed = $englishAllListPassed -and $myanmarAllListPassed -and $chineseAllListPassed -and $japaneseAllListPassed
$allTestsPassed = $allHistoryPassed -and $allAllListPassed

if ($allTestsPassed) {
    Write-Host "🎉 ALL TRANSLATION TESTS PASSED!" -ForegroundColor Green
    Write-Host "✅ History page translation: COMPLETE" -ForegroundColor Green
    Write-Host "✅ All List page translation: COMPLETE" -ForegroundColor Green
    Write-Host "✅ Multi-language support: WORKING" -ForegroundColor Green
} else {
    Write-Host "⚠️ Some translation tests failed." -ForegroundColor Yellow
    Write-Host "Please check the failing components and fix translation issues." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Test completed! Check the results above." -ForegroundColor Cyan
