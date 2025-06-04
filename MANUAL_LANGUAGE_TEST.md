# Manual Language Switching Test

## Current Status
- App should be running on device/emulator
- Logcat monitoring is active for HSU, Language, Translation, Exception, and Error logs

## Test Steps

### 1. Open Language Settings
- Open navigation drawer (hamburger menu)
- Tap "Settings" / "ဆက်တင်များ"
- Look for language dropdown

### 2. Test Japanese Language Switch
- Select "日本語 (Japanese)" from dropdown
- **Expected:** Toast message appears
- **Expected:** UI immediately updates to Japanese
- **Expected:** Navigation drawer menu items change to Japanese
- Check main screen buttons and text

### 3. Test Chinese Language Switch  
- Select "中文 (Chinese)" from dropdown
- **Expected:** Toast message appears
- **Expected:** UI immediately updates to Chinese
- **Expected:** Navigation drawer menu items change to Chinese
- Check main screen buttons and text

### 4. Test Language Persistence
- Switch to Japanese or Chinese
- Close app completely
- Reopen app
- **Expected:** App opens in the last selected language

## What to Report

### If Working Correctly:
- Toast appears when language is selected
- All UI elements translate immediately
- Language persists after restart

### If Not Working:
- No toast appears
- UI doesn't change language
- App crashes when selecting language
- Language reverts to English after restart

## Debugging Info Being Collected
- All language switching events in logcat
- Any exceptions or errors during language changes
- Translation loading messages

## Next Steps
After testing, check the logcat output for any errors or issues.
