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
![Lines of Code](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/loc.json)
![Modules](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/modules.json)
![Screens](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/screens.json)
![CI Workflows](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/workflows.json)

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

## Destinations (To be moved to documentation)

Cycling destinations are stored as JSON files in the app assets and loaded into a Room database at runtime based on the user's location.

### How It Works

1. On launch, the app gets the user's location
2. The file resolver scans asset files and selects those whose center point is within **100 km** of the user
3. Selected files are parsed and inserted into the local database (each file is loaded only once)
4. The app picks random destinations from the database within a target distance range

### File Format

Each destination file is a JSON array of objects with 4 fields:

| Field   | Type   | Description                           |
|---------|--------|---------------------------------------|
| `id`    | String | Unique identifier (`{city}-{number}`) |
| `title` | String | Display name, local name              |
| `lat`   | Double | Latitude                              |
| `long`  | Double | Longitude                             |

Example:

```json
[
  { "id": "tokyo-001", "title": "Imperial Palace Loop (皇居周回)", "lat": 35.6840, "long": 139.7528 },
  { "id": "tokyo-002", "title": "Yoyogi Park", "lat": 35.6728, "long": 139.6949 }
]
```

### File Naming Convention

```
destinations_{city}_{country}_{centerLat}_{centerLon}_tier{N}.json
```

- **city** — lowercase city name (e.g., `tokyo`, `hiroshima`)
- **country** — lowercase country name (e.g., `japan`)
- **centerLat / centerLon** — geographic center of the file's coverage area
- **tier** — integer indicating coverage zone (see below)

Example: `destinations_tokyo_japan_35.6812_139.7671_tier1.json`

### Tiers

Tiers organize destinations by distance from the city center. Lower tiers are loaded first.

| Tier | Coverage        | ID range example   |
|------|-----------------|--------------------|
| 1    | City center     | `tokyo-001` – `050` |
| 2    | Suburban areas  | `tokyo-051` – `100` |
| 3    | Wider region    | `tokyo-101` +       |

### Adding a New City

1. Create a JSON file in `feature/destinations/src/main/assets/` following the naming convention
2. Use a unique city prefix for IDs (e.g., `osaka-001`)
3. Set `centerLat` / `centerLon` in the filename to the city center coordinates
4. Start with tier 1 (city center destinations) and add higher tiers as needed
5. Build and run — the app will automatically discover and load the new file when a user is within 100 km
