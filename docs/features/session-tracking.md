# Session Tracking

The session feature manages the cycling session lifecycle with a foreground service for background tracking, real-time location updates, and notification controls.

## Components

| Component                   | Purpose                                       |
|-----------------------------|-----------------------------------------------|
| `SessionTrackingService`    | Foreground service with `START_STICKY`         |
| `SessionTracker`            | Tracking orchestrator (delegates from service) |
| `SessionNotificationManager`| Notification with pause/resume/stop actions   |
| `SessionServiceController`  | Interface to start service from ViewModel      |

## Use Cases

| Use Case                      | Purpose                              |
|-------------------------------|--------------------------------------|
| `ActiveSessionUseCase`        | Observe/get active session           |
| `CreateSessionUseCase`        | Create new session                   |
| `UpdateSessionStatusUseCase`  | Pause/resume/stop/onServiceRestart   |
| `UpdateSessionLocationUseCase`| Add track points                     |
| `UpdateSessionPowerUseCase`   | Process power meter readings         |

## Service Flow

```
SessionViewModel
    │ starts service on session create
    ▼
SessionTrackingService (foreground, type=location)
    └── delegates to SessionTrackerImpl
            ├── Observes ActiveSessionUseCase
            ├── Collects location every 3s / 5m
            ├── Collects power meter readings (with exponential backoff retry)
            ├── Monitors location enabled state (auto-pause)
            ├── Updates notification every 1s
            ├── Handles notification actions (pause/resume)
            └── Observes NutritionReminderUseCase (triggers vibration)
```

The service runs as a **foreground service** with `START_STICKY` and `foregroundServiceType=location`, ensuring tracking continues even with the screen off. `SessionTrackingService` is a thin delegate — all tracking logic lives in `SessionTrackerImpl`, which owns the coroutine scope and all background jobs.

### Concurrency

`UpdateSessionStatusUseCase`, `UpdateSessionLocationUseCase`, and `UpdateSessionPowerUseCase` share a `Mutex` singleton (via `@SessionMutex` Hilt qualifier) to serialize concurrent state mutations, preventing races between status changes, location updates, and power readings.

## Location Processing

### Location Smoothing

GPS data passes through a Kalman-filter-based smoother to reduce noise and improve route accuracy. The `LocationSmoother` is registered as `@Provides` with no scope in Hilt (stateful per session), not `@Singleton`.

### Location Validation

Invalid or inaccurate location points are filtered out before processing, preventing erratic jumps in the tracked route.

### Speed Buffering

Speed values are averaged over a sliding window for stable readouts. The `UpdateSessionLocationUseCase` maintains an internal speed buffer that resets per segment via `saveSegmentStartPoint()`.

## Notification Controls

The session notification provides direct controls:

- **Pause** — pause the active session
- **Resume** — resume a paused session
- **Stop** — stop the session and navigate to completion

Notification updates happen every 1 second to show current duration, distance, and speed.

## Power Tracking

When a paired power meter with session usage enabled is available, `SessionTrackerImpl` automatically connects and streams power data during the session.

### Session Power Fields

| Field                 | Type      | Description                          |
|-----------------------|-----------|--------------------------------------|
| `totalPowerReadings`  | `Int?`    | Count of readings received           |
| `sumPowerWatts`       | `Long?`   | Sum of all power values              |
| `maxPowerWatts`       | `Int?`    | Peak power recorded                  |
| `totalEnergyJoules`   | `Double?` | Accumulated energy                   |
| `averagePowerWatts`   | `Int?`    | Computed: `sumPowerWatts / totalPowerReadings` |

All fields are nullable — sessions without a power meter have no power data.

### Retry Strategy

BLE connections can drop during a ride. Power collection uses exponential backoff to reconnect automatically:

- Initial delay: 2 seconds, max delay: 5 minutes, factor: 2x
- Successful reading resets delay to initial value
- Power collection starts on `RUNNING`, stops on `PAUSED`/`COMPLETED`

See [Power Meter](power-meter.md) for the full power meter architecture.
