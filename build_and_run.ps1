# PowerShell script to build and run the app
Write-Host "Starting Android App Build and Run Process" -ForegroundColor Green

# Navigate to project directory
Set-Location "c:\Users\EBPMyanmar\AndroidStudioProjects\MyApplication"

# Function to check if emulator is ready
function Test-EmulatorReady {
    $devices = adb devices
    return $devices -match "emulator.*device"
}

# Function to kill Java processes
function Stop-JavaProcesses {
    Write-Host "Stopping Java processes..." -ForegroundColor Yellow
    Get-Process -Name "java*" -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 2
}

# Function to clean build directory
function Remove-BuildDirectory {
    Write-Host "Cleaning build directory..." -ForegroundColor Yellow
    if (Test-Path "app\build") {
        Remove-Item "app\build" -Recurse -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# Main execution
try {
    # Check emulator
    if (-not (Test-EmulatorReady)) {
        Write-Host "Error: No Android emulator detected. Please start an emulator first." -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Emulator detected and ready!" -ForegroundColor Green
    
    # Clean previous build artifacts
    Stop-JavaProcesses
    Remove-BuildDirectory
    
    # Build the app
    Write-Host "Building the app..." -ForegroundColor Cyan
    $buildResult = & .\gradlew.bat assembleDebug --no-daemon --stacktrace
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Build successful!" -ForegroundColor Green
        
        # Install the app
        Write-Host "Installing app on emulator..." -ForegroundColor Cyan
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        
        if (Test-Path $apkPath) {
            adb install -r $apkPath
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Installation successful!" -ForegroundColor Green
                
                # Launch the app
                Write-Host "Launching app..." -ForegroundColor Cyan
                adb shell am start -n com.hsu.expense/.MainActivity
                Write-Host "App launched successfully!" -ForegroundColor Green
            } else {
                Write-Host "Installation failed!" -ForegroundColor Red
            }
        } else {
            Write-Host "APK file not found at: $apkPath" -ForegroundColor Red
        }
    } else {
        Write-Host "Build failed! Check the output above for errors." -ForegroundColor Red
    }
} catch {
    Write-Host "An error occurred: $_" -ForegroundColor Red
}

Write-Host "Process completed. Press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
