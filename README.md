# CyclingAssistant

An Android app that helps cyclists discover cycling destinations. Features include destination
selection, session tracking, and real-time location updates.

![Coverage](https://img.shields.io/badge/coverage-0%25-red)

## Tech Stack

| Category                 | Technology                   |
|--------------------------|------------------------------|
| **Language**             | Kotlin 2.3                   |
| **UI Framework**         | Jetpack Compose (Material 3) |
| **Architecture**         | MVVM + Clean Architecture    |
| **Dependency Injection** | Koin 4.1                     |
| **Async**                | Kotlin Coroutines & Flow     |
| **Database**             | Room 2.8                     |
| **Preferences**          | DataStore                    |
| **Navigation**           | Navigation Compose 2.9       |
| **Maps**                 | Google Maps Compose 7.0      |
| **Location**             | Play Services Location       |
| **Testing**              | JUnit 4, MockK, Turbine      |
| **Code Quality**         | Detekt                       |
| **Coverage**             | Kover                        |
| **Build**                | Gradle 8.13, AGP 8.13        |
| **Min SDK**              | 24 (Android 7.0)             |
| **Target SDK**           | 36                           |

## Features

- **Destination Discovery** - Browse and select cycling destinations
- **Session Tracking** - Track cycling sessions with real-time stats
- **Background Service** - Foreground service for continuous tracking
- **Route Visualization** - View traveled route on map
- **Session History** - Review past cycling sessions
- **Share Results** - Share session summaries as images
- **Theme Support** - Light and dark theme support

## Build

```bash
./gradlew build           # Build the project
./gradlew test            # Run unit tests
./gradlew detektRun       # Run lint checks
./gradlew koverXmlReport  # Generate coverage report
./gradlew installDebug    # Install on device
```

## Setup

1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Build and run the app