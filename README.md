# CyclingAssistant

An Android app that helps cyclists discover cycling destinations.
Features include randomized destination selection, session tracking, and real-time location updates.

The project also serves as an attempt to prove and a test of writing a production ready application from scratch via an AI Agent giving it only architectural directions with the minimal code editing.

## License

This project is dual-licensed:
- Free for non-commercial and educational use
- Commercial use requires a separate license
- Use of this code for training AI/ML models is explicitly prohibited

See [LICENSE](LICENSE) for details.

## Technical Approach

Multi-module Clean Architecture with MVVM pattern and unidirectional data flow. Features are isolated into independent modules and communicate through the bridge pattern.

![Unit Test Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/coverage.json)

See [Module Dependency Graph](docs/MODULE_GRAPH.md) for visual representation.

## Features

- **Destination Discovery** — browse and select cycling destinations with randomized suggestions
- **Session Tracking** — track cycling sessions with real-time stats (distance, speed, duration)
- **Route Visualization** — view the traveled route on an interactive map
- **Session History** — review past cycling sessions and their details
- **Share Results** — share session summaries as images
- **Nutrition Tracking** — track nutrition data linked to cycling sessions
- **Theme Support** — light and dark theme
- **Language Support** — English, Russian, and Japanese
- **User Profile** — personalized settings and preferences

### Known Issues

- Google Maps is not rendered on devices with outdated Google Play Services

## Technical Highlights

### Architecture

- **Multi-module Clean Architecture** — strict layer separation (presentation → domain → data) within each feature module
- **Bridge Pattern** — cross-feature communication through dedicated bridge API/impl modules, keeping features decoupled
- **MVVM** — ViewModels expose `StateFlow<UiState>` for persistent state and `Channel`-based `Flow` for one-time navigation events

### Data Persistence

- **Room** — local database for destinations, sessions, and track points
- **DataStore** — user preferences (theme, language, profile)

### Data Processing

- **Location Smoothing** — Kalman-filter-based smoothing for GPS data to reduce noise and improve route accuracy
- **Location Validation** — filtering out invalid or inaccurate location points
- **Speed Buffering** — averaging speed values over a sliding window for stable readouts

### Background Operation

- **Foreground Service** — `START_STICKY` service with `location` type for continuous tracking even with screen off
- **Notification Controls** — pause, resume, and stop session directly from the notification

### CI/CD

- **Automated Testing** — unit tests and Detekt checks run on every push to `main` and on pull requests
- **Code Coverage** — Kover generates coverage reports;
- **Coverage Badge** — dynamically updated on each push to `main`
- **Build Cache** — dedicated pipeline pre-builds and caches artifacts on `main` to speed up subsequent runs
- **Module Graph** — on-demand workflow to regenerate the module dependency graph
- **Dependency Management** — Dependabot creates weekly grouped PRs for Kotlin, AndroidX, testing, and GitHub Actions updates

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

## Working with the Project

### Setup

1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Build and run the app

### Build

```bash
./gradlew build           # Build the project
./gradlew test            # Run unit tests
./gradlew detektRun       # Run lint checks
./gradlew koverXmlReport  # Generate coverage report
./gradlew installDebug    # Install on device
```
