@echo off
echo ========================================
echo   MyApplication - Build and Run Helper
echo ========================================
echo.
echo IMPORTANT: If you encounter file lock errors, please:
echo 1. Close Android Studio completely
echo 2. Restart your computer
echo 3. Run this script again
echo.
echo Attempting to build and run the app...
echo.

REM First, try to kill any processes that might be locking files
echo Stopping any running Java/Gradle processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
timeout /t 2 /nobreak >nul

REM Try to build without cleaning first
echo.
echo Step 1: Building APK (without clean)...
call gradlew assembleDebug --no-daemon --offline 2>nul
if %errorlevel% neq 0 (
    echo.
    echo Build failed. Trying alternative approach...
    echo.
    
    REM If that fails, try with daemon
    echo Step 1b: Building with daemon...
    call gradlew assembleDebug
    if %errorlevel% neq 0 (
        echo.
        echo ❌ Build failed! Please try the following:
        echo.
        echo SOLUTION 1:
        echo 1. Close Android Studio completely
        echo 2. Restart your computer
        echo 3. Run this script again
        echo.
        echo SOLUTION 2:
        echo 1. Open Android Studio
        echo 2. Use Build → Clean Project
        echo 3. Use Build → Rebuild Project
        echo 4. Use Run → Run 'app'
        echo.
        pause
        exit /b 1
    )
)

echo.
echo ✅ Build successful!
echo.

REM Check for connected devices
echo Step 2: Checking for Android devices...
adb devices 2>nul | findstr "device" | findstr -v "List" >nul
if %errorlevel% neq 0 (
    echo.
    echo ⚠️  No Android device/emulator detected.
    echo.
    echo Please:
    echo 1. Start an Android emulator, OR
    echo 2. Connect an Android device with USB debugging enabled
    echo.
    echo Once a device is ready, press any key to continue...
    pause >nul
    
    REM Check again
    adb devices 2>nul | findstr "device" | findstr -v "List" >nul
    if %errorlevel% neq 0 (
        echo Still no device found. Please set up a device and try again.
        pause
        exit /b 1
    )
)

echo Device found!
echo.

REM Install the APK
echo Step 3: Installing APK...
call adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo.
    echo ❌ Installation failed!
    echo.
    echo This might be because:
    echo 1. The APK wasn't built successfully
    echo 2. USB debugging is not enabled on your device
    echo 3. Device storage is full
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Installation successful!
echo.

REM Launch the app
echo Step 4: Launching MyApplication...
call adb shell am start -n com.example.myapplication/.MainActivity
if %errorlevel% neq 0 (
    echo.
    echo ❌ Failed to launch app automatically.
    echo Please launch the app manually from your device.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  🎉 SUCCESS! App is now running!
echo ========================================
echo.
echo The app should now be open on your device/emulator.
echo.
echo Features implemented:
echo ✅ Dark/Light mode theme switching
echo ✅ Expense list shows name + price only
echo ✅ Tap items to open detail view
echo ✅ Edit/delete functionality
echo ✅ Modern UI with theme-aware colors
echo.
echo Enjoy testing your Hsu Expense!
echo.
pause
