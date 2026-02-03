# CyclingAssistant

An Android app that helps cyclists discover cycling destinations.  
Features include randomized destination selection, session tracking, and real-time location updates.

![Unit Test Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/coverage.json)

The project also serves as an attempt to prove and a test of writing a production ready application from scratch via an AI Agent giving it only architectural directions with the minimal code editing.

## License
This project is dual-licensed:
- Free for non-commercial and educational use
- Commercial use requires a separate license

## Planned work
### Features
- Baseline profiles
- UI tests run on CI
- CD setup on CI 
### Known issues
- Theme names in the drop down are hardcoded in English
- Google map is not rendered for devices with outdated Google Play Services

## Tech Stack

| Category                 | Technology                   |
|--------------------------|------------------------------|
| **Language**             | Kotlin                       |
| **UI Framework**         | Jetpack Compose (Material 3) |
| **Architecture**         | MVVM + Clean Architecture    |
| **Dependency Injection** | Koin                         |
| **Async**                | Kotlin Coroutines & Flow     |
| **Database**             | Room                         |
| **Preferences**          | DataStore                    |
| **Navigation**           | Navigation Compose           |
| **Maps**                 | Google Maps Compose          |
| **Location**             | Play Services Location       |
| **Testing**              | JUnit 4, MockK, Turbine      |
| **Code Quality**         | Detekt                       |
| **Coverage**             | Kover                        |
| **Build**                | Gradle, AGP                  |
| **Min SDK**              | 24 (Android 7.0)             |
| **Target SDK**           | 36                           |
| **CI**                   | Github Actions               |
| **Dependencies update**  | Dependabot                   |

## Architecture

Multi-module Clean Architecture with feature modules and bridge pattern for cross-feature communication.

See [Module Dependency Graph](docs/MODULE_GRAPH.md) for visual representation.

## Features

- **Destination Discovery** - Browse and select cycling destinations
- **Session Tracking** - Track cycling sessions with real-time stats
- **Background Service** - Foreground service for continuous tracking
- **Route Visualization** - View traveled route on map
- **Session History** - Review past cycling sessions
- **Share Results** - Share session summaries as images
- **Theme Support** - Light and dark theme support
- **Languages Support** - English, Russian and Japanese support

## Setup
1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Build and run the app

## Build
```bash
./gradlew build           # Build the project
./gradlew test            # Run unit tests
./gradlew detektRun       # Run lint checks
./gradlew koverXmlReport  # Generate coverage report
./gradlew installDebug    # Install on device
```
