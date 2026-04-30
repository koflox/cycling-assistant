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
│   ├── session/                      # Composite feature split into bounded sub-modules:
│   │   ├── completion/               # Completed-session screen
│   │   ├── data/                     # Room/DataStore (sessions + stats display config)
│   │   ├── domain/                   # Pure-Kotlin: entities, repos, use cases
│   │   ├── history/                  # Sessions list
│   │   ├── init/                     # Hilt providers for domain UseCases (mirrors :shared:init)
│   │   ├── nav-graph/                # `sessionGraph` aggregator wired into AppNavHost
│   │   ├── route-render/             # Shared map/route components + SessionUiMapper
│   │   ├── share/                    # Share dialog (image/GPX/Strava tabs)
│   │   ├── stats-display/            # Stats display config screen
│   │   └── tracking/                 # Active session UI + foreground service
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
    ├── init/                         # Hilt providers for pure-Kotlin shared modules
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
| `init`            | Hilt providers for pure-Kotlin shared modules ([init pattern](#init-and-nav-graph-modules)) |
| `location`        | Location services, validation, smoothing           |
| `map`             | Google Maps route rendering constants & utilities  |
| `sensor-protocol` | BLE sensor data parsing (cycling power measurement)|
| `testing`         | Shared test utilities (`MainDispatcherRule`, etc)  |

## `init` and `nav-graph` modules

Two patterns we use when a feature is split into multiple sub-modules.

### `init` modules — Hilt bootstrap for pure-Kotlin code

Pure-Kotlin modules (`org.jetbrains.kotlin.jvm`) cannot host Hilt `@Module` classes — Hilt's KSP processor needs an Android library context. An `init` module is a thin Android library that exists solely to register `@Provides` for types defined in pure-Kotlin siblings (and any small Android-only utilities those siblings need at the feature level).

- **`:shared:init`** registers providers for `:shared:altitude`, `:shared:distance`, `:shared:id` and `CurrentTimeProvider`.
- **`:feature:session:init`** registers providers for the entire `:feature:session:domain` UseCase surface and hosts `SessionErrorMessageMapper` (used by VMs across `completion` and `tracking`).

App depends on `init` modules so Hilt's KSP discovers the `@Module` classes. Consumers of the providers (other modules) do not need to depend on `init` directly — Hilt resolves the bindings at the application graph level.

### `nav-graph` modules — navigation aggregation across sub-features

When a feature is split into several presentation sub-modules, each sub-module exposes a public `Route` composable. A `nav-graph` module composes those into a single `NavGraphBuilder` extension (e.g., `sessionGraph`) that the app's `NavHost` calls once.

- **`:feature:session:nav-graph`** composes routes from `:completion`, `:history`, `:share`, and `:stats-display` into `sessionGraph`. App calls `sessionGraph(navController, ...)` from `AppNavHost`.

The `nav-graph` module is the only place in the feature that knows about `NavController` and the route templates; sub-features stay navigation-agnostic and just expose `Route` composables.

## Module Dependency Graph

The full dependency graph is auto-generated by CI on each push to `main`. View the standalone page: [Module Graph](../MODULE_GRAPH.md).

--8<-- "docs/MODULE_GRAPH.md:3:"
