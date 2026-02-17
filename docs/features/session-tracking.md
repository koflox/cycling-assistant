# Session Tracking

The session feature manages the cycling session lifecycle with a foreground service for background tracking, real-time location updates, and notification controls.

## Components

| Component                   | Purpose                                       |
|-----------------------------|-----------------------------------------------|
| `SessionTrackingService`    | Foreground service with `START_STICKY`         |
| `SessionNotificationManager`| Notification with pause/resume/stop actions   |
| `SessionServiceController`  | Interface to start/stop service from ViewModel |

## Use Cases

| Use Case                      | Purpose                     |
|-------------------------------|-----------------------------|
| `ActiveSessionUseCase`        | Observe/get active session  |
| `CreateSessionUseCase`        | Create new session          |
| `UpdateSessionStatusUseCase`  | Pause/resume/stop           |
| `UpdateSessionLocationUseCase`| Add track points            |

## Service Flow

```
SessionViewModel
    │ starts service on session create
    ▼
SessionTrackingService (foreground, type=location)
    ├── Observes ActiveSessionUseCase
    ├── Collects location every 3s
    ├── Updates notification every 1s
    └── Handles notification actions
```

The service runs as a **foreground service** with `START_STICKY` and `foregroundServiceType=location`, ensuring tracking continues even with the screen off.

## Location Processing

### Location Smoothing

GPS data passes through a Kalman-filter-based smoother to reduce noise and improve route accuracy. The `LocationSmoother` is registered as a `factory` in Koin (stateful per session), not a `single`.

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
