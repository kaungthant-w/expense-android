@echo off
echo Building MyApplication (bypassing clean)...
echo.

echo Step 1: Building APK directly...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Step 2: Installing APK...
adb devices | findstr "device" >nul
if %errorlevel% neq 0 (
    echo No Android device/emulator found. Please start an emulator or connect a device.
    pause
    exit /b 1
)

call adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo Installation failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Launching app...
call adb shell am start -n com.example.myapplication/.MainActivity
if %errorlevel% neq 0 (
    echo Failed to launch app!
    pause
    exit /b 1
)

echo.
echo ✅ App built and launched successfully!
pause
