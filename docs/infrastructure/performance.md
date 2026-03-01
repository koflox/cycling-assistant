# Performance

Catalog of performance techniques used across the codebase.

## GPS & Location

### Kalman Filter Smoothing

GPS data passes through a Kalman-filter-based `LocationSmoother` to reduce noise and improve route accuracy. The smoother is registered as a Koin `factory` (stateful per session) — each new session gets a fresh instance with reset filter state.

### Location Accuracy Validation

`LocationValidator` filters out inaccurate location points before processing. Invalid readings (low accuracy, zero coordinates, unrealistic jumps) are discarded, preventing erratic route artifacts.

### Altitude Gain Threshold

Altitude changes smaller than 1 meter are ignored to filter out GPS vertical noise. Only sustained altitude changes above the threshold contribute to total gain/loss calculations.

## Speed Processing

### Median Buffer

`UpdateSessionLocationUseCase` maintains a speed buffer (size 5) that computes the median of recent speed readings. This filters out momentary GPS spikes while preserving genuine speed changes. The buffer resets per segment via `saveSegmentStartPoint()`.

### Acceleration Clamping

Speed changes are clamped to a maximum acceleration of 8 km/h/s. Readings that would require physically impossible acceleration are rejected, preventing sudden jumps from GPS errors.

## Concurrency

### ConcurrentFactory (Double-Checked Locking)

`ConcurrentFactory<T>` provides a coroutine-safe lazy initialization pattern with `Mutex`-based double-checked locking. The `@Volatile` instance field enables fast-path reads without lock contention after first initialization. Used by `RoomDatabaseFactory` and all DAO factories.

See [Concurrency](concurrency.md) for full implementation details.

### Mutex Serialization

Session state mutations (`UpdateSessionStatusUseCase`, `UpdateSessionLocationUseCase`, `UpdateSessionPowerUseCase`) share a single `Mutex` via `SessionQualifier.SessionMutex`. This serializes concurrent updates from location collection, notification actions, and power readings without blocking threads.

### SupervisorJob for Independent Lifecycles

`SessionTrackerImpl` uses a `SupervisorJob`-based scope so that independent jobs (location collection, notification timer, nutrition reminders, power collection) can fail independently without cancelling siblings. A power meter disconnect does not affect location tracking.

### Dispatcher Strategy

Each architectural layer uses a dedicated dispatcher to match its workload:

| Layer      | Dispatcher          | Rationale                    |
|------------|---------------------|------------------------------|
| ViewModel  | `dispatcherDefault` | CPU-bound state transforms   |
| Mapper     | `dispatcherDefault` | CPU-bound data mapping       |
| Repository | delegates           | Coordinates, minimal own work|
| DataSource | `dispatcherIo`      | Disk and network I/O         |

### lazyUnsafe

`lazy(mode = LazyThreadSafetyMode.NONE)` shorthand for cases where thread safety is guaranteed by the call site (e.g., single-threaded access). Avoids synchronization overhead of the default `SYNCHRONIZED` mode.

## BLE Connectivity

### Exponential Backoff Retry

Power meter collection uses exponential backoff for reconnection after BLE disconnects:

| Parameter        | Value     |
|------------------|-----------|
| Initial delay    | 2 seconds |
| Maximum delay    | 5 minutes |
| Backoff factor   | 2x        |
| Reset on success | Yes       |

On successful reading, the delay resets to the initial value. Only `PowerConnectionException` triggers retry — `CancellationException` propagates normally to respect structured concurrency.

See [Concurrency](concurrency.md#exponential-backoff-retry) for pseudocode.

## UI Responsiveness

### Click Debouncing

`DebouncedButton` and `DebouncedOutlinedButton` (from `shared/design-system`) enforce a 400ms debounce interval, preventing duplicate actions from rapid taps.

### distinctUntilChanged on Location Settings

Location-enabled state observation uses `distinctUntilChanged()` to avoid redundant recompositions and state changes when the system broadcasts duplicate values.

### Mutually Exclusive GPS Subscriptions

Only one GPS subscription is active at a time — the session tracker and destination discovery do not compete for location updates. The active session takes priority and the foreground service owns the subscription.

## Build & APK

### Multi-Module Parallel Builds

The project's 40+ Gradle modules enable parallel compilation. Independent modules build simultaneously, significantly reducing incremental build times. Only changed modules and their dependents recompile.

### R8 Minification & Resource Shrinking

Release builds use R8 for code minification and resource shrinking, reducing APK size and removing unused code paths. ProGuard rules are maintained per module where needed.
