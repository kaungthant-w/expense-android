@echo off
echo Testing Language Settings Implementation...
echo.

echo ========================================
echo LANGUAGE IMPLEMENTATION TEST
echo ========================================
echo.

echo 1. Checking if language JSON files exist...
if exist "app\src\main\assets\lang\strings_en.json" (
    echo ✓ English strings file found
) else (
    echo ✗ English strings file missing
)

if exist "app\src\main\assets\lang\strings_mm.json" (
    echo ✓ Myanmar strings file found
) else (
    echo ✗ Myanmar strings file missing
)

echo.
echo 2. Checking if LanguageManager class exists...
if exist "app\src\main\java\com\example\myapplication\LanguageManager.kt" (
    echo ✓ LanguageManager.kt found
) else (
    echo ✗ LanguageManager.kt missing
)

echo.
echo 3. Checking if LanguageActivity class exists...
if exist "app\src\main\java\com\example\myapplication\LanguageActivity.kt" (
    echo ✓ LanguageActivity.kt found
) else (
    echo ✗ LanguageActivity.kt missing
)

echo.
echo 4. Checking if language layout exists...
if exist "app\src\main\res\layout\activity_language.xml" (
    echo ✓ activity_language.xml found
) else (
    echo ✗ activity_language.xml missing
)

echo.
echo 5. Building and installing app...
call gradlew assembleDebug >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ App built successfully
    call gradlew installDebug >nul 2>&1
    if %errorlevel% == 0 (
        echo ✓ App installed successfully
    ) else (
        echo ✗ App installation failed
    )
) else (
    echo ✗ App build failed
)

echo.
echo 6. Starting app...
adb shell am start -n com.example.myapplication/.MainActivity
if %errorlevel% == 0 (
    echo ✓ App started successfully
) else (
    echo ✗ Failed to start app
)

echo.
echo ========================================
echo MANUAL TESTING INSTRUCTIONS:
echo ========================================
echo 1. Open the app on your device/emulator
echo 2. Tap the hamburger menu (☰) at the top
echo 3. Select "Settings"
echo 4. You should see a "🌐 Language Settings" card
echo 5. Tap on the Language Settings card
echo 6. You should see two radio buttons:
echo    - 🇺🇸 English
echo    - 🇲🇲 မြန်မာ
echo 7. Try switching between languages
echo 8. The app should restart when you change language
echo 9. Verify that the interface changes language
echo.
echo ========================================
echo TEST COMPLETE
echo ========================================
pause
