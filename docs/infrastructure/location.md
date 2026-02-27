# Location Updates

## Update Intervals

The app uses different location update intervals depending on the component:

| Component | Interval | Min Distance | Purpose |
|-----------|----------|-------------|---------|
| `LocationDelegate` (idle) | 15 000 ms | 50 m | Map marker during idle |
| `SessionTrackerImpl` (active) | 3 000 ms | 5 m | Session route tracking |

## Mutually Exclusive Subscriptions

`LocationDelegate` and `SessionTrackerImpl` never subscribe to GPS concurrently. During an active
session, the session tracker is the sole GPS subscriber. The destinations map derives the user's
current location from the route data's last position instead:

1. `listenToActiveSession()` stops `LocationDelegate` observation when a session starts and
   restarts it when the session ends.
2. `observeActiveSessionRoute()` feeds `routeData.lastPosition` into
   `locationDelegate.updateUserLocation()`, keeping the map marker in sync without a separate GPS
   subscription.
3. `onScreenResumed()` and `onPermissionGranted()` only start idle observation when no session is
   active.

This avoids two concurrent GPS callbacks with overlapping purpose and reduces battery usage.

## Key Files

| File | Purpose |
|------|---------|
| `feature/destinations/.../delegate/LocationDelegate.kt` | Idle location observation, user location state |
| `feature/destinations/.../RideMapViewModel.kt` | Stop/start orchestration, route-to-location bridging |
| `feature/session/service/SessionTracker.kt` | Session tracking location collection |
| `shared/location/.../LocationDataSource.kt` | Location data source interface (no defaults) |
