# Setup Instructions

## Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK API 24+
- Firebase account

## Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project or select existing
3. Add an Android app with package name: `com.sixseven.app`
4. Download `google-services.json`
5. Replace `app/google-services.json` with your downloaded file
6. Enable **Realtime Database** in Firebase Console
7. Set database rules:

```json
{
  "rules": {
    "leaderboard": {
      ".read": true,
      ".write": true
    }
  }
}
```

## Sound Files

Add two MP3 files to `app/src/main/res/raw/`:
- `sound_up.mp3` - Upward motion sound
- `sound_down.mp3` - Downward motion sound

## Build

```bash
./gradlew assembleDebug
```

## Install on Device

```bash
./gradlew installDebug
```

## Testing

1. Install app on physical Android device
2. Grant required permissions
3. Hold phone and move it up and down
4. Sounds should play and score should increment
