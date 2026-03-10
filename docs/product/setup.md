# Setup

## Getting Started

1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Place your Firebase `google-services.json` in `app/` (see [Firebase Setup](#firebase))
4. Build and run the app

## Build Commands

```bash
./gradlew build           # Build the project
./gradlew test            # Run unit tests
./gradlew detektRun       # Run lint checks
./gradlew koverXmlReport  # Generate coverage report
./gradlew installDebug    # Install on device
```

## API Keys

Google Maps requires an API key configured in `secrets.properties` at the project root. This file is git-ignored.

```properties
MAPS_API_KEY=your_key_here
```

## Firebase

The project uses Firebase Crashlytics and Performance Monitoring. Place your `google-services.json` in the `app/` directory. This file is git-ignored.

Download it from the [Firebase Console](https://console.firebase.google.com/) → Project Settings → Android app. The file must contain client entries for all three package variants:

- `com.koflox.cyclingassistant` (release)
- `com.koflox.cyclingassistant.debug` (debug)
- `com.koflox.cyclingassistant.staging` (staging)

For CI setup, see [CI/CD — Setup Secrets](../infrastructure/ci-cd.md#setup-secrets-setup-secrets).

## Localization

The app supports three languages:

| Language | Resource directory |
|----------|--------------------|
| English  | `values/` (default) |
| Russian  | `values-ru/`       |
| Japanese | `values-ja/`       |

All user-facing strings are defined in `res/values/strings.xml`. When adding or modifying strings, always add translations to all supported locales.
