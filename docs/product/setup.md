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
./gradlew build                              # Build the project
./gradlew test                               # Run unit tests
./gradlew detektRun                          # Run lint checks
./gradlew koverXmlReport                     # Generate coverage report
./gradlew installDebug                       # Install on device
./gradlew :module:recordRoborazziDebug       # Record screenshot golden images
./gradlew :module:verifyRoborazziDebug       # Verify screenshots against golden images
```

## API Keys

API keys live in `secrets.properties` at the project root. The file is git-ignored. See
[`secrets.properties.example`](https://github.com/koflox/cycling-assistant/blob/main/secrets.properties.example)
for the full template.

### Google Maps

Required for the in-app map. Enable **Maps SDK for Android** in your Google Cloud project.

```properties
MAPS_API_KEY=your_key_here
```

### Strava

Required for the [Strava Sync](../features/strava-sync.md) feature. Register an OAuth
application at [strava.com/settings/api](https://www.strava.com/settings/api).

| Strava console field | Value |
|---|---|
| Authorization Callback Domain | `koflox.github.io` |
| Application Website | any valid URL |

```properties
STRAVA_CLIENT_ID=your_client_id
STRAVA_CLIENT_SECRET=your_client_secret
```

The redirect URI is hard-coded to `cyclingassistant://koflox.github.io/strava/callback` (matched
by the deep-link `intent-filter` on `StravaOAuthRedirectActivity`). New Strava apps are limited
to **1 connected athlete** by default — request a quota increase via Strava's developer console
once the app is ready to onboard real users.

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
