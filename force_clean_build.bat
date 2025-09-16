@echo off
echo Force cleaning build directory...

REM Kill any Java processes that might be holding files
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
timeout /t 2

REM Remove build directory with retries
echo Attempting to remove build directory...
for /L %%i in (1,1,5) do (
    echo Attempt %%i...
    rmdir /s /q "app\build" 2>nul
    if not exist "app\build" (
        echo Build directory successfully removed!
        goto :build
    )
    timeout /t 3
)

:build
echo Starting build...
gradlew.bat assembleDebug --no-daemon --max-workers=1

if %ERRORLEVEL% EQU 0 (
    echo Build successful! Installing APK...
    adb install -r "app\build\outputs\apk\debug\app-debug.apk"
    if %ERRORLEVEL% EQU 0 (
        echo Installation successful! Launching app...
        adb shell am start -n com.hsu.expense/.MainActivity
    ) else (
        echo Installation failed. Please check if device is connected.
    )
) else (
    echo Build failed. Please check Android Studio for detailed errors.
)

pause
