# MaaserCalculator

Android app for tracking maaser (tithing) in Hebrew/English.

## Setup
1. Add your `google-services.json` to the `app/` directory (or set the `GOOGLE_SERVICES_JSON` GitHub secret for CI).
2. Add Rubik font files to `app/src/main/res/font/`:
   - `rubik_regular.ttf`
   - `rubik_medium.ttf`
   - `rubik_bold.ttf`
3. Run `./gradlew assembleDebug`

## Structure
- `data/` — Room DB, DataStore, Firestore
- `domain/usecase/` — Business logic
- `ui/` — Compose screens & ViewModels
- `widget/` — Glance home screen widget
- `di/` — Hilt dependency injection modules
