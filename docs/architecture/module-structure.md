# Module Structure

The project is organized into three module categories: **app**, **feature**, and **shared**.

## Module Tree

```
CyclingAssistant/
├── app/                              # Shell — navigation, theme, Hilt bootstrap, Room DB
├── build-logic/                      # Convention plugins (cycling.feature, cycling.library, etc.)
├── feature/
│   ├── bridge/                       # Cross-feature communication (alphabetical pair names)
│   │   ├── connection-session/       # connections ↔ session
│   │   ├── destination-nutrition/    # destinations ↔ nutrition
│   │   ├── destination-poi/          # destinations ↔ poi
│   │   ├── destination-session/      # destinations ↔ session
│   │   ├── nutrition-session/        # nutrition ↔ session
│   │   ├── nutrition-settings/       # nutrition ↔ settings
│   │   ├── poi-settings/             # poi ↔ settings
│   │   ├── profile-session/          # profile ↔ session
│   │   ├── session-settings/         # session ↔ settings
│   │   └── session-strava/           # session ↔ strava (GPX data + activity name)
│   │       ├── api/                  # Interfaces exposed to consumers
│   │       └── impl/                 # Implementations wiring to provider internals
│   ├── connections/                  # BLE device connection and management
│   ├── dashboard/                    # Main dashboard with expandable menu
│   ├── destinations/                 # Destination selection feature
│   ├── integrations/
│   │   └── strava/                   # Strava OAuth, GPX upload, sync state (api + impl)
│   ├── locale/                       # App language persistence and observation
│   ├── nutrition/                    # Nutrition tracking and reminders
│   ├── poi/                          # POI type selection and active session POI actions
│   ├── profile/                      # Rider profile management
│   ├── sensor/
│   │   └── power/                    # Power meter test mode and observation
│   ├── session/                      # Session tracking, stats display, GPX export
│   ├── settings/                     # App settings (theme, language, stats display)
│   └── theme/                        # App theme persistence and observation
└── shared/
    ├── altitude/                     # Altitude gain calculator
    ├── ble/                          # BLE primitives (GATT, scanning, permissions)
    ├── concurrent/                   # Coroutine dispatchers, suspendRunCatching, ConcurrentFactory
    ├── design-system/                # UI theme, colors, spacing, components
    ├── di/                           # Hilt qualifier annotations
    ├── distance/                     # Distance calculator
    ├── error/                        # Error mapping utilities
    ├── graphics/                     # Bitmap utilities
    ├── id/                           # ID generator
    ├── location/                     # Location services (validator, smoother, data sources)
    ├── map/                          # Google Maps route rendering constants & utilities
    ├── sensor-protocol/              # BLE sensor data parsing (cycling power)
    └── testing/                      # Test utilities
```

### app

The shell module — owns the `AppDatabase` (Room), `AppNavHost` navigation wiring, theme setup, and Hilt bootstrap (`@HiltAndroidApp`). Every feature and shared module is included here.

### feature

Each feature module contains `di/`, `domain/`, `data/`, and `presentation/` packages. Features are fully isolated and communicate only through [bridge modules](bridge-pattern.md).

Bridge modules live under `feature/bridge/` and are named as alphabetically-ordered pairs of the two features they connect (e.g., `destination-session`, not `session-destination`). Each bridge has an `api/` submodule (interfaces) and an `impl/` submodule (wiring to the provider).

### shared

Utility modules consumed by features:

| Module            | Purpose                                            |
|-------------------|----------------------------------------------------|
| `altitude`        | Altitude gain calculator                           |
| `ble`             | BLE primitives (GATT, scanning, state, permissions)|
| `concurrent`      | Coroutine dispatchers, `suspendRunCatching`, `ConcurrentFactory` |
| `design-system`   | Material 3 theme, colors, spacing, components      |
| `di`              | Hilt qualifier annotations                         |
| `distance`        | Distance calculator                                |
| `error`           | Error mapping utilities                            |
| `graphics`        | Bitmap utilities                                   |
| `id`              | ID generator                                       |
| `location`        | Location services, validation, smoothing           |
| `map`             | Google Maps route rendering constants & utilities  |
| `sensor-protocol` | BLE sensor data parsing (cycling power measurement)|
| `testing`         | Shared test utilities (`MainDispatcherRule`, etc)  |

## Module Dependency Graph

The full dependency graph is auto-generated by CI on each push to `main`. View the standalone page: [Module Graph](../MODULE_GRAPH.md).

--8<-- "docs/MODULE_GRAPH.md:3:"
