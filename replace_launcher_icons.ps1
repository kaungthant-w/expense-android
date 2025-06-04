# PowerShell script to replace Android launcher icons with image_for_android.jpg
# This script converts and resizes the image to all required Android icon sizes

param(
    [string]$SourceImage = "image_for_android.jpg",
    [string]$ProjectRoot = "."
)

# Color functions for better output
function Write-Success { param($msg) Write-Host $msg -ForegroundColor Green }
function Write-Info { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host $msg -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host $msg -ForegroundColor Red }

# Check if source image exists
if (-not (Test-Path $SourceImage)) {
    Write-Error "Source image '$SourceImage' not found!"
    exit 1
}

Write-Info "=== Android Launcher Icon Replacement Script ==="
Write-Info "Source image: $SourceImage"

# Define Android icon sizes and their corresponding folders
$iconSizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

# Base path to resources
$resPath = Join-Path $ProjectRoot "app\src\main\res"

# Check if res folder exists
if (-not (Test-Path $resPath)) {
    Write-Error "Android res folder not found at: $resPath"
    exit 1
}

# Function to check if ImageMagick is available
function Test-ImageMagick {
    try {
        $null = magick -version 2>$null
        return $true
    } catch {
        return $false
    }
}

# Function to convert using ImageMagick
function Convert-WithImageMagick {
    param($source, $output, $size)
    
    $cmd = "magick convert `"$source`" -resize ${size}x${size}^ -gravity center -extent ${size}x${size} `"$output`""
    Invoke-Expression $cmd
    return $?
}

# Function to convert using .NET (fallback)
function Convert-WithDotNet {
    param($source, $output, $size)
    
    try {
        Add-Type -AssemblyName System.Drawing
        
        $image = [System.Drawing.Image]::FromFile((Resolve-Path $source).Path)
        $bitmap = New-Object System.Drawing.Bitmap($size, $size)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        
        $graphics.DrawImage($image, 0, 0, $size, $size)
        
        $bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
        
        $graphics.Dispose()
        $bitmap.Dispose()
        $image.Dispose()
        
        return $true
    } catch {
        Write-Warning "Failed to convert with .NET: $_"
        return $false
    }
}

# Check conversion method
$useImageMagick = Test-ImageMagick
if ($useImageMagick) {
    Write-Success "Using ImageMagick for conversion"
} else {
    Write-Warning "ImageMagick not found, using .NET fallback"
}

# Process each icon size
$successCount = 0
$totalCount = $iconSizes.Count

foreach ($folder in $iconSizes.Keys) {
    $size = $iconSizes[$folder]
    $targetFolder = Join-Path $resPath $folder
    $outputFile = Join-Path $targetFolder "ic_launcher.png"
    
    Write-Info "Processing $folder (${size}x${size})..."
    
    # Create folder if it doesn't exist
    if (-not (Test-Path $targetFolder)) {
        New-Item -ItemType Directory -Path $targetFolder -Force | Out-Null
        Write-Info "Created folder: $targetFolder"
    }
    
    # Convert image
    $success = $false
    if ($useImageMagick) {
        $success = Convert-WithImageMagick $SourceImage $outputFile $size
    } else {
        $success = Convert-WithDotNet $SourceImage $outputFile $size
    }
    
    if ($success -and (Test-Path $outputFile)) {
        Write-Success "✓ Generated: $outputFile"
        $successCount++
        
        # Also create round icon
        $roundIconFile = Join-Path $targetFolder "ic_launcher_round.png"
        if ($useImageMagick) {
            Convert-WithImageMagick $SourceImage $roundIconFile $size | Out-Null
        } else {
            Convert-WithDotNet $SourceImage $roundIconFile $size | Out-Null
        }
        
        if (Test-Path $roundIconFile) {
            Write-Success "✓ Generated: $roundIconFile"
        }    } else {
        Write-Error "✗ Failed to generate: $outputFile"
    }
}

# Summary
Write-Info "`n=== Summary ==="
Write-Success "Successfully generated: $successCount/$totalCount icon sizes"

if ($successCount -eq $totalCount) {
    Write-Success "All launcher icons have been replaced successfully!"
    Write-Info "You can now build and install your app to see the new icon."
} else {
    Write-Warning "Some icons failed to generate. Check the errors above."
}

# Optional: Build and install
$buildChoice = Read-Host "`nWould you like to build and install the app now? (y/n)"
if ($buildChoice -eq 'y' -or $buildChoice -eq 'Y') {
    Write-Info "Building and installing app..."
    
    # Build debug APK
    & ".\gradlew.bat" assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Build successful!"
        
        # Install APK
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            adb install $apkPath
            Write-Success "App installed! Check your device for the new icon."
        }
    } else {
        Write-Error "Build failed!"
    }
}
