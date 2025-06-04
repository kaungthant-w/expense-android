# Simple script to copy image to Android icon folders
Write-Host "Replacing Android launcher icons..." -ForegroundColor Cyan

# Check if source image exists
if (-not (Test-Path "image_for_android.jpg")) {
    Write-Host "Error: image_for_android.jpg not found!" -ForegroundColor Red
    exit 1
}

# Create directories and copy files
$folders = @("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")
$resPath = "app\src\main\res"

foreach ($folder in $folders) {
    $targetPath = Join-Path $resPath $folder
    
    # Create directory if it doesn't exist
    if (-not (Test-Path $targetPath)) {
        New-Item -ItemType Directory -Path $targetPath -Force | Out-Null
        Write-Host "Created directory: $targetPath" -ForegroundColor Green
    }
    
    # Copy image as PNG (will need manual conversion)
    $iconPath = Join-Path $targetPath "ic_launcher.png"
    $roundPath = Join-Path $targetPath "ic_launcher_round.png"
    
    try {
        Copy-Item "image_for_android.jpg" $iconPath -Force
        Copy-Item "image_for_android.jpg" $roundPath -Force
        Write-Host "Copied to: $folder" -ForegroundColor Green
    } catch {
        Write-Host "Failed to copy to: $folder" -ForegroundColor Red
    }
}

Write-Host "Icon replacement completed!" -ForegroundColor Green
Write-Host "Note: JPG files were copied as PNG. Android may still display them, but for best results, convert to PNG format." -ForegroundColor Yellow
