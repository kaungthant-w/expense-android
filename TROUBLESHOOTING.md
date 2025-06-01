# TROUBLESHOOTING GUIDE - MyApplication HsuPar Expense

## 🚨 FILE LOCK ERRORS (Current Issue)

**Problem**: Build fails with "Unable to delete directory" or "Couldn't delete R.jar"
**Cause**: File handles are locked by background processes

### SOLUTION 1: Quick Fix
1. **Close ALL Android Studio windows completely**
2. **Restart your computer** (this clears all file locks)
3. **Run `run_myapp.bat` script**

### SOLUTION 2: Manual Process Kill
1. Open Task Manager (Ctrl + Shift + Esc)
2. End all processes containing:
   - `java.exe`
   - `javaw.exe` 
   - `gradle`
   - `Android Studio`
3. Wait 30 seconds
4. Try building again

### SOLUTION 3: Use Android Studio IDE
1. Open Android Studio
2. File → Open → Select your project
3. Build → Clean Project
4. Build → Rebuild Project  
5. Run → Run 'app'

## 🛠️ BUILD ALTERNATIVES

### Option A: Use the new script
```cmd
.\run_myapp.bat
```

### Option B: Manual commands
```cmd
# Stop all gradle daemons
.\gradlew --stop

# Wait 10 seconds, then build
.\gradlew assembleDebug --no-daemon

# Install on device
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Launch app
adb shell am start -n com.example.myapplication/.MainActivity
```

## 📱 DEVICE SETUP

### For Android Emulator:
1. Open Android Studio
2. Tools → AVD Manager
3. Create/Start an emulator
4. Wait for it to fully boot

### For Physical Device:
1. Enable Developer Options:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect via USB
4. Allow USB Debugging when prompted

## ✅ TESTING THE APP

Once successfully installed:

### 1. Theme Testing
- Change device theme: Settings → Display → Dark theme
- App colors should switch automatically

### 2. Basic Functionality
- Add expenses using the form
- Only name and price should show in the list
- Tap any expense item → should open detail view

### 3. Detail Activity Testing  
- Edit expense details
- Save changes → should return to main list
- Delete expense → should show confirmation → remove from list

### 4. Navigation Testing
- Tap expense items to navigate
- Use back button to return
- Check that changes persist

## 📋 IMPLEMENTATION STATUS

✅ **COMPLETED FEATURES:**
- Dark/Light mode theme system
- Expense list with name + price only
- Click navigation to detail activity
- Complete edit/delete functionality
- Input validation and error handling
- Date/time pickers
- Confirmation dialogs
- Toast notifications
- Result handling between activities

## 🎯 EXPECTED BEHAVIOR

1. **Main Screen**: Clean list showing expense names and prices
2. **Item Click**: Opens detail screen with all expense data
3. **Edit Mode**: Modify any field, save returns to main list
4. **Delete Mode**: Confirmation dialog, then removes item
5. **Theme Switch**: All colors change appropriately

## 💡 TIPS

- If build issues persist, restart computer (most effective)
- Use Android Studio IDE as backup method
- Check device storage if installation fails  
- Ensure only one Android IDE/emulator is running
- Clear gradle cache if needed: Delete `.gradle` folder in user home

Your HsuPar Expense app is fully implemented and ready to test!
