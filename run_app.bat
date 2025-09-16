@echo off
echo Building and running MyApplication...
echo.

echo Step 1: Cleaning project...
call gradlew clean
if errorlevel 1 goto error

echo.
echo Step 2: Building debug APK...
call gradlew assembleDebug
if errorlevel 1 goto error

echo.
echo Step 3: Installing on emulator...
call gradlew installDebug
if errorlevel 1 goto error

echo.
echo Step 4: Launching app...
adb shell am start -n com.hsu.expense/.MainActivity

echo.
echo ✅ App successfully built and launched!
echo You can now test your dark/light mode features in the emulator.
echo.
goto end

:error
echo.
echo ❌ Build failed! Please check the error messages above.
echo.
pause

:end
