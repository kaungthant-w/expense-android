# Simple PowerShell script to replace Android launcher icons
param(
    [string]$SourceImage = "image_for_android.jpg"
)

Write-Host "=== Android Icon Replacement Script ===" -ForegroundColor Cyan
Write-Host "Source image: $SourceImage" -ForegroundColor Green

# Check if source image exists
if (-not (Test-Path $SourceImage)) {
    Write-Host "Error: Source image '$SourceImage' not found!" -ForegroundColor Red
    exit 1
}

# Define icon sizes
$iconSizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

# Base path
$resPath = "app\src\main\res"

# Check if ImageMagick is available
function Test-ImageMagick {
    try {
        $result = cmd /c "magick -version 2>nul"
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    }
}

# Convert using ImageMagick
function Convert-WithImageMagick($source, $output, $size) {
    try {
        cmd /c "magick `"$source`" -resize ${size}x${size} `"$output`""
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    }
}

# Convert using .NET
function Convert-WithDotNet($source, $output, $size) {
    try {
        Add-Type -AssemblyName System.Drawing
        $image = [System.Drawing.Image]::FromFile((Resolve-Path $source).Path)
        $bitmap = New-Object System.Drawing.Bitmap($size, $size)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.DrawImage($image, 0, 0, $size, $size)
        $bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
        $graphics.Dispose()
        $bitmap.Dispose()
        $image.Dispose()
        return $true
    } catch {
        Write-Host "Error with .NET conversion: $_" -ForegroundColor Yellow
        return $false
    }
}

# Check conversion method
$useImageMagick = Test-ImageMagick
if ($useImageMagick) {
    Write-Host "Using ImageMagick for conversion" -ForegroundColor Green
} else {
    Write-Host "Using .NET for conversion" -ForegroundColor Yellow
}

$successCount = 0
$totalCount = $iconSizes.Count

# Process each size
foreach ($folder in $iconSizes.Keys) {
    $size = $iconSizes[$folder]
    $targetFolder = Join-Path $resPath $folder
    $outputFile = Join-Path $targetFolder "ic_launcher.png"
    
    Write-Host "Processing $folder (${size}x${size})..." -ForegroundColor Cyan
    
    # Create folder if needed
    if (-not (Test-Path $targetFolder)) {
        New-Item -ItemType Directory -Path $targetFolder -Force | Out-Null
    }
    
    # Convert image
    $success = $false
    if ($useImageMagick) {
        $success = Convert-WithImageMagick $SourceImage $outputFile $size
    } else {
        $success = Convert-WithDotNet $SourceImage $outputFile $size
    }
    
    if ($success -and (Test-Path $outputFile)) {
        Write-Host "✓ Generated: $outputFile" -ForegroundColor Green
        $successCount++
        
        # Also create round icon
        $roundIconFile = Join-Path $targetFolder "ic_launcher_round.png"
        if ($useImageMagick) {
            Convert-WithImageMagick $SourceImage $roundIconFile $size | Out-Null
        } else {
            Convert-WithDotNet $SourceImage $roundIconFile $size | Out-Null
        }
    } else {
        Write-Host "✗ Failed to generate: $outputFile" -ForegroundColor Red
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Successfully generated: $successCount/$totalCount icon sizes" -ForegroundColor Green

if ($successCount -eq $totalCount) {
    Write-Host "All launcher icons replaced successfully!" -ForegroundColor Green
} else {
    Write-Host "Some icons failed to generate." -ForegroundColor Yellow
}
