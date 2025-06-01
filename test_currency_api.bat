@echo off
echo ====================
echo Testing Currency API Fix
echo ====================
echo.

echo Starting Android Emulator...
adb start-server

echo.
echo Waiting for device...
adb wait-for-device

echo.
echo Opening Currency Exchange Activity...
adb shell am start -n com.example.myapplication/.CurrencyExchangeActivity

echo.
echo Instructions for manual testing:
echo 1. Open the Currency Exchange screen
echo 2. Tap "Fetch Latest Rate" button
echo 3. Check if real USD rate appears (should be around 4000-5000 MMK)
echo 4. Tap "Refresh" button in the exchange rates table
echo 5. Verify all currency rates show real data from Myanmar Currency API
echo.
echo Expected real rates (approximate):
echo - USD: 4000-5000 MMK
echo - EUR: 4500-5000 MMK  
echo - SGD: 3000-3500 MMK
echo - MYR: 900-1000 MMK
echo - CNY: 600-700 MMK
echo - THB: 120-130 MMK
echo - JPY: 27-30 MMK
echo.

echo Checking device logs for API responses...
echo Press Ctrl+C to stop log monitoring
echo.
adb logcat -s CurrencyApiService
