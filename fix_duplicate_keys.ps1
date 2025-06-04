# Fix duplicate keys in JSON translation files

function Remove-DuplicateKeys {
    param(
        [string]$FilePath
    )
    
    Write-Host "Processing: $FilePath" -ForegroundColor Yellow
    
    # Read all lines
    $lines = Get-Content $FilePath -Encoding UTF8
    $processedKeys = @{}
    $cleanedLines = @()
    $inJsonObject = $false
    
    foreach ($line in $lines) {
        # Check if we're entering the JSON object
        if ($line.Trim() -eq "{") {
            $inJsonObject = $true
            $cleanedLines += $line
            continue
        }
        
        # Check if we're exiting the JSON object
        if ($line.Trim() -eq "}") {
            $inJsonObject = $false
            $cleanedLines += $line
            continue
        }
        
        # If we're inside the JSON object, check for key duplicates
        if ($inJsonObject -and $line -match '^\s*"([^"]+)":') {
            $key = $matches[1]
            
            if ($processedKeys.ContainsKey($key)) {
                Write-Host "  Removing duplicate key: $key" -ForegroundColor Red
                continue  # Skip this line (remove duplicate)
            } else {
                $processedKeys[$key] = $true
                $cleanedLines += $line
            }
        } else {
            $cleanedLines += $line
        }
    }
    
    # Write back to file
    $cleanedLines | Set-Content $FilePath -Encoding UTF8
    Write-Host "  Fixed: $FilePath" -ForegroundColor Green
}

# Fix Japanese file
$jaFile = "C:\Users\EBPMyanmar\AndroidStudioProjects\MyApplication\app\src\main\assets\lang\strings_ja.json"
Remove-DuplicateKeys -FilePath $jaFile

# Fix Chinese file  
$zhFile = "C:\Users\EBPMyanmar\AndroidStudioProjects\MyApplication\app\src\main\assets\lang\strings_zh.json"
Remove-DuplicateKeys -FilePath $zhFile

Write-Host "`nVerifying fixes..." -ForegroundColor Cyan

# Verify Japanese file
Write-Host "`nJapanese file duplicates:" -ForegroundColor Yellow
Get-Content $jaFile | ForEach-Object { if ($_ -match '^\s*"([^"]+)":') { $matches[1] } } | Group-Object | Where-Object { $_.Count -gt 1 } | Select-Object Name, Count

# Verify Chinese file
Write-Host "`nChinese file duplicates:" -ForegroundColor Yellow
Get-Content $zhFile | ForEach-Object { if ($_ -match '^\s*"([^"]+)":') { $matches[1] } } | Group-Object | Where-Object { $_.Count -gt 1 } | Select-Object Name, Count

Write-Host "`nDuplicate key cleanup completed!" -ForegroundColor Green
