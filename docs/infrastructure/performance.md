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

## Baseline Profiles & Macrobenchmarks

### What Are Baseline Profiles?

Baseline Profiles are lists of classes and methods that the Android Runtime (ART) pre-compiles (AOT) at install time, avoiding JIT compilation on first launch. This reduces cold start latency and eliminates jank during critical user journeys.

The project uses the [Baseline Profile Gradle Plugin](https://developer.android.com/topic/performance/baselineprofiles/overview) (`androidx.baselineprofile`) to generate and bundle profiles automatically.

### Module Structure

```
baselineprofile/                              # com.android.test module
├── AppInteractions.kt                        # Shared MacrobenchmarkScope extensions
├── BaselineProfileGenerator.kt               # Profile generation tests
├── StartupBenchmarks.kt                      # Startup timing benchmarks
├── NavigationBenchmarks.kt                   # Navigation frame timing benchmarks
└── ScrollBenchmarks.kt                       # Scroll frame timing benchmarks
```

### Profile Types

| Profile | Purpose | Generated by |
|---------|---------|--------------|
| **Startup Profile** (`startup-prof.txt`) | Pre-compiled at install, covers app launch → dashboard render | `generateStartupProfile()` |
| **Baseline Profile** (`baseline-prof.txt`) | Compiled in background after install, covers critical user journeys | `generateCriticalUserJourneyProfile()` |

Generated profiles are stored in `app/src/release/generated/baselineProfiles/` and must be committed to the repository. The `profileinstaller` library applies them on the device.

### Generating Profiles

Requires a device or emulator (API 28+, ideally API 33+):

```bash
./gradlew :app:generateReleaseBaselineProfile
```

The plugin builds a `nonMinifiedRelease` variant, installs it, runs the profile generators, and writes the output profiles.

### Benchmark Metrics

**StartupTimingMetric** — measures app launch latency:

- `timeToInitialDisplayMs`: time from intent to first frame rendered (TTID)

**FrameTimingMetric** — measures rendering performance:

- `frameDurationCpuMs`: CPU time to process a single frame. At 60 FPS the budget is ~16.7 ms, at 120 FPS ~8.3 ms.
- `frameOverrunMs`: how much a frame exceeded (positive) or beat (negative) its deadline. Negative = frame finished early. Positive = janky frame (dropped).

### Running Benchmarks

Run all benchmarks on a physical device:

```bash
./gradlew :baselineprofile:connectedBenchmarkAndroidTest
```

Run a specific benchmark class:

```bash
./gradlew :baselineprofile:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.koflox.baselineprofile.StartupBenchmarks
```

Results are written to:

```
baselineprofile/build/outputs/connected_android_test_additional_output/
  benchmarkRelease/connected/<device>/com.koflox.baselineprofile-benchmarkData.json
```

### Benchmark Coverage

**Startup benchmarks** (10 iterations each, `StartupTimingMetric`):

| Test | CompilationMode | StartupMode |
|------|----------------|-------------|
| `startupColdNoCompilation` | `None` | COLD |
| `startupColdBaselineProfiles` | `Partial(Require)` | COLD |
| `startupWarmBaselineProfiles` | `Partial(Require)` | WARM |
| `startupHotBaselineProfiles` | `Partial(Require)` | HOT |

**Navigation benchmarks** (5 iterations each, `FrameTimingMetric`, `Partial(Require)`):

- `navigateToSettings` — dashboard → settings → back
- `navigateToConnections` — dashboard → connections → back
- `navigateToSessions` — dashboard → sessions → back

**Scroll benchmarks** (5 iterations each, `FrameTimingMetric`, `Partial(Require)`):

- `scrollStatsConfig` — settings → stats display config → scroll

### Benchmark Results

Measured on **Samsung Galaxy S22 Ultra (SM-S908B)**, Android 16 (API 36), 8-core Exynos 2200, 120 Hz display, 11 GB RAM. CPU clocks not locked (dynamic frequency scaling, production-like conditions).

**Startup latency** (`timeToInitialDisplayMs`):

| Test | Min | Median | Max |
|------|-----|--------|-----|
| Cold — No Compilation | 270 ms | **308 ms** | 323 ms |
| Cold — Baseline Profiles | 265 ms | **284 ms** | 319 ms |
| Warm — Baseline Profiles | 97 ms | **121 ms** | 190 ms |
| Hot — Baseline Profiles | 51 ms | **63 ms** | 74 ms |

Cold start improvement with Baseline Profiles: **~8% (308 → 284 ms median)**.

**Frame timing** (`frameDurationCpuMs` / `frameOverrunMs`, deadline ~8.3 ms at 120 Hz):

| Test | Duration P50 | Overrun P50 | Overrun P99 |
|------|-------------|-------------|-------------|
| Navigate → Connections | 3.9 ms | -1.2 ms | 22.2 ms |
| Navigate → Sessions | 3.9 ms | -1.2 ms | 20.9 ms |
| Navigate → Settings | 3.7 ms | -1.4 ms | 36.3 ms |
| Scroll Stats Config | 3.9 ms | -2.5 ms | 19.3 ms |

Negative overrun P50 means frames finish well ahead of deadline. P99 spikes are expected during screen transitions (first-frame composition cost).

### TestTags for UI Automation

UiAutomator identifies Compose elements via `testTag`. Constants are centralized in `shared/design-system/testtag/TestTags.kt` and shared with the benchmark module via `implementation(project(":shared:design-system"))`.

The root composable in `MainActivity` enables UiAutomator access to testTags:

```kotlin
Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) { ... }
```

### Permissions

Benchmarks grant runtime permissions programmatically via `grantPermissions()` before each test iteration using `pm grant` shell commands. This prevents permission dialogs from blocking UI automation.

### CI Verification

The `baseline-profiles.yml` workflow runs on PRs targeting `main` and validates:

| Check | What it verifies |
|-------|-----------------|
| **Source profiles exist** | `baseline-prof.txt` and `startup-prof.txt` are committed and non-empty |
| **Profiles bundled in AAB** | `baseline.prof` and `baseline.profm` present in release AAB |
| **Startup DEX layout** | `r8.json` metadata has `"startup": true` for `classes.dex` — confirms `dexLayoutOptimization` placed startup classes in the first DEX file |
| **SHA-256 validation** | DEX checksums in AAB match checksums in `r8.json` — confirms no tool interfered with R8 output after profile compilation |
| **Binary profile size** | Warning if compiled `baseline.prof` exceeds 1.5 MB (large profiles increase install time) |

The startup DEX layout check and SHA-256 validation use [bundle metadata](https://developer.android.com/topic/performance/baselineprofiles/confirm-startup-profiles#confirm-bundle-metadata) introduced in AGP 8.8.
