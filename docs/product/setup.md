# Setup

## Getting Started

1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Build and run the app

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

## Localization

The app supports three languages:

| Language | Resource directory |
|----------|--------------------|
| English  | `values/` (default) |
| Russian  | `values-ru/`       |
| Japanese | `values-ja/`       |

All user-facing strings are defined in `res/values/strings.xml`. When adding or modifying strings, always add translations to all supported locales.
