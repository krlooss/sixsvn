# Six Seven - Android Game

Motion-based game app for the "6 7" trend movement.

## Features

- Accelerometer-based motion detection
- Sound effects for up/down movements
- Haptic feedback
- Score tracking with local best score
- Global leaderboard with Firebase

## Setup

1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app with package name `com.sixseven.app`
3. Download `google-services.json` and replace the placeholder file in `app/`
4. Enable Firebase Realtime Database in your Firebase console
5. Add sound files `sound_up.mp3` and `sound_down.mp3` to `app/src/main/res/raw/`

## Sound Files

Place two sound files in `app/src/main/res/raw/`:
- `sound_up.mp3` - Plays when lifting phone
- `sound_down.mp3` - Plays when lowering phone

## Build

```bash
./gradlew assembleDebug
```

## Database Rules

Set these rules in Firebase Realtime Database:

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
