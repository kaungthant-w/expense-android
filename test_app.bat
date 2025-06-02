@echo off
echo Testing Hsu Expense App Features...
echo.

echo 1. Taking screenshot of main activity...
adb shell screencap -p /sdcard/main_screen.png
adb pull /sdcard/main_screen.png screenshots/main_screen.png

echo.
echo 2. Testing FAB menu (tapping FAB button)...
adb shell input tap 540 960
timeout /t 2 /nobreak > nul

echo 3. Taking screenshot of FAB menu...
adb shell screencap -p /sdcard/fab_menu.png
adb pull /sdcard/fab_menu.png screenshots/fab_menu.png

echo.
echo 4. Testing Summary activity (tapping Summary button)...
adb shell input tap 405 810
timeout /t 3 /nobreak > nul

echo 5. Taking screenshot of Summary activity...
adb shell screencap -p /sdcard/summary_screen.png
adb pull /sdcard/summary_screen.png screenshots/summary_screen.png

echo.
echo 6. Going back to main activity...
adb shell input keyevent 4
timeout /t 2 /nobreak > nul

echo 7. Testing History activity...
adb shell input tap 540 960
timeout /t 1 /nobreak > nul
adb shell input tap 540 810
timeout /t 3 /nobreak > nul

echo 8. Taking screenshot of History activity...
adb shell screencap -p /sdcard/history_screen.png
adb pull /sdcard/history_screen.png screenshots/history_screen.png

echo.
echo 9. Going back to main activity...
adb shell input keyevent 4
timeout /t 2 /nobreak > nul

echo 10. Testing Analytics activity...
adb shell input tap 540 960
timeout /t 1 /nobreak > nul
adb shell input tap 675 810
timeout /t 3 /nobreak > nul

echo 11. Taking screenshot of Analytics activity...
adb shell screencap -p /sdcard/analytics_screen.png
adb pull /sdcard/analytics_screen.png screenshots/analytics_screen.png

echo.
echo Testing completed! Screenshots saved to screenshots folder.
echo.
pause
