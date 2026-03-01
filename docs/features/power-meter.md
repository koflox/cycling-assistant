# Power Meter

BLE-based power meter connectivity for real-time wattage and cadence during cycling sessions.

## Overview

The power meter feature allows cyclists to connect BLE cycling power meters (Bluetooth Cycling Power Service, UUID `0x1818`) and stream real-time power and cadence data. Power readings integrate into active sessions, providing average/max power, cadence RPM, and energy expenditure. A standalone test mode lets users verify device connectivity before riding.

## Architecture

```mermaid
graph TD
    BLE[shared:ble] --> SP[shared:sensor-protocol]
    SP --> FSP[feature:sensor:power]
    FSP --> FC[feature:connections]
    FC --> Bridge[bridge:connection-session]
    Bridge --> FS[feature:session]

    BLE -.- BleGattManager
    BLE -.- BleScanner
    SP -.- CyclingPowerParser
    SP -.- CadenceCalculator
    FSP -.- ObservePowerDataUseCase
    FC -.- PairedDevice / DeviceListScreen
    Bridge -.- SessionPowerMeterUseCase
    FS -.- SessionTracker / UpdateSessionPowerUseCase
```

## BLE Infrastructure (`shared/ble`)

Low-level BLE primitives reused by any feature needing Bluetooth connectivity.

| Component              | Purpose                                                                 | DI Scope  |
|------------------------|-------------------------------------------------------------------------|-----------|
| `BleGattManager`       | Manages a single GATT connection — connect, disconnect, enable notifications | `factory` |
| `BleScanner`           | Scans for devices matching service UUIDs with timeout and deduplication | `single`  |
| `BluetoothStateMonitor`| Observes system Bluetooth on/off state via `BroadcastReceiver`          | `single`  |
| `BlePermissionChecker` | Returns required permissions per API level, checks grant status         | `single`  |

### GATT Event Flow

`BleGattManager.connect(address)` returns a cold `Flow<BleGattEvent>`:

1. Opens a GATT connection to the MAC address
2. Emits `ConnectionStateChanged(CONNECTED)` on successful connect
3. Auto-discovers services, emits `ServicesDiscovered`
4. After notification subscription, emits `CharacteristicChanged(serviceUuid, characteristicUuid, data)` for each notification
5. Emits `ConnectionStateChanged(DISCONNECTED)` and completes on disconnect

Implementation wraps `BluetoothGattCallback` in a `callbackFlow`, handling both Tiramisu+ and legacy Android APIs.

### Permissions

| API Level | Required Permissions                           |
|-----------|------------------------------------------------|
| 31+ (S)   | `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`          |
| < 31      | `ACCESS_FINE_LOCATION`                         |

## Sensor Protocol (`shared/sensor-protocol`)

Pure Kotlin parsers for BLE cycling power data — no Android dependencies.

### CyclingPowerParser

Parses raw byte arrays from the Cycling Power Measurement characteristic (`0x2A63`):

- 2-byte flags (little-endian)
- 2-byte instantaneous power (watts)
- If `FLAG_CRANK_REVOLUTION_DATA_PRESENT` (`0x0020`) is set: 2-byte cumulative crank revolutions + 2-byte last crank event time

Returns a `CyclingPowerMeasurement(instantaneousPowerWatts, crankRevolutions?, lastCrankEventTime?)`.

### CadenceCalculator

**Stateful** calculator that derives RPM from consecutive crank revolution/event-time pairs:

- Formula: `RPM = (deltaRevs / deltaTime) * 60 * 1024` (time in 1/1024s units)
- Handles 16-bit counter wraparound via `and 0xFFFF`
- Returns `null` for the first reading, stationary crank, or zero time delta
- DI scope: `factory` (one per connection, since it tracks previous readings)

## Power Observation (`feature/sensor/power`)

### ObservePowerDataUseCase

Combines `BleGattManager` + `CyclingPowerParser` + `CadenceCalculator` into a single observable pipeline:

1. Connects to device via `BleGattManager.connect(macAddress)`
2. Waits for `ServicesDiscovered`, then enables notifications on the power measurement characteristic
3. Filters for `CharacteristicChanged` events, parses with `CyclingPowerParser`
4. Calculates cadence via `CadenceCalculator`
5. Emits `PowerReading(timestampMs, powerWatts, cadenceRpm?)`

DI scope: `factory` (both `BleGattManager` and `CadenceCalculator` are stateful per connection).

Wraps BLE errors in `PowerMeterConnectionException`.

### Power Test Mode

Standalone screen for verifying power meter connectivity before a ride:

- **Route:** `power_test_mode/{mac_address}`
- Shows live power gauge, cadence RPM, rolling chart (last 60 readings), and accumulated stats (avg/max power, calories)
- Checks Bluetooth state before connecting — shows `BluetoothDisabled` overlay if off
- Reconnect/disconnect controls

## Device Management (`feature/connections`)

### Domain

| Model         | Fields                                                             |
|---------------|--------------------------------------------------------------------|
| `PairedDevice`| `id`, `macAddress`, `name`, `deviceType`, `isSessionUsageEnabled`  |
| `DeviceType`  | `POWER_METER` (extensible for future sensor types)                 |

The `isSessionUsageEnabled` flag controls whether a device is automatically used during cycling sessions.

**Use Cases:**

| Use Case                         | Purpose                                      |
|----------------------------------|----------------------------------------------|
| `SavePairedDeviceUseCase`        | Save new device (duplicate MAC check)        |
| `ObservePairedDevicesUseCase`    | Observe all paired devices                   |
| `DeletePairedDeviceUseCase`      | Delete a paired device                       |
| `UpdateDeviceSessionUsageUseCase`| Toggle session usage flag                    |

### Data

- **Room entity:** `PairedDeviceEntity` (table: `paired_devices`) — `id` (PK), `macAddress`, `name`, `deviceType`, `isSessionUsageEnabled`
- **DAO:** `PairedDeviceDao` — observable query ordered by name, standard CRUD operations
- Data access via `ConcurrentFactory<PairedDeviceDao>` with `ConnectionsQualifier.DaoFactory` qualifier

### Presentation

**Device List Screen:**

- `LazyColumn` with `SwipeToDismissBox` for swipe-to-delete
- Session usage toggle per device
- FAB for adding new devices
- Delete confirmation dialog
- Navigates to test mode or scanning

**BLE Scanning Sheet:**

- `ModalBottomSheet` scanning for Cycling Power Service devices (30s timeout)
- Marks already-saved devices in scan results
- `BlePermissionHandler` for runtime permission requests (Accompanist)
- On selection: saves device as `DeviceType.POWER_METER`

**Navigation:** Nested graph (`connections_graph`) containing device list, scanning, and power test mode screens.

## Session Integration

### Bridge (`connection-session`)

`SessionPowerMeterUseCase` (API) exposes three operations:

| Method                               | Purpose                                             |
|--------------------------------------|-----------------------------------------------------|
| `getSessionPowerDevice()`            | Find first session-enabled power meter device       |
| `observePowerReadings(macAddress)`    | Stream `PowerReadingData` from device               |
| `disconnect()`                       | Disconnect from power meter                         |

The impl combines `ObservePairedDevicesUseCase` (connections) with `ObservePowerDataUseCase` (sensor/power).

### UpdateSessionPowerUseCase

Processes incoming power readings within an active session:

1. Acquires the shared session `Mutex` (same one used by location and status use cases)
2. Verifies session is `RUNNING`
3. Calculates energy delta: `powerWatts * (currentTimestamp - lastTimestamp) / 1000.0` joules
4. Updates session fields and saves

**Session power fields:**

| Field                 | Type      | Description                          |
|-----------------------|-----------|--------------------------------------|
| `totalPowerReadings`  | `Int?`    | Count of readings received           |
| `sumPowerWatts`       | `Long?`   | Sum of all power values              |
| `maxPowerWatts`       | `Int?`    | Peak power recorded                  |
| `totalEnergyJoules`   | `Double?` | Accumulated energy                   |
| `averagePowerWatts`   | `Int?`    | Computed: `sumPowerWatts / totalPowerReadings` |

All fields are nullable — sessions without a power meter have no power data.

### Retry Strategy

`SessionTrackerImpl` uses exponential backoff for power meter reconnection:

| Parameter       | Value       |
|-----------------|-------------|
| Initial delay   | 2 seconds   |
| Max delay       | 5 minutes   |
| Backoff factor  | 2x          |
| Reset on success| Yes         |

Power collection starts when session status is `RUNNING` and stops (with disconnect) on `PAUSED`, `COMPLETED`, or service stop. Only `PowerConnectionException` triggers retry — cancellation propagates normally.

See also: [Session Tracking](session-tracking.md) for broader session architecture, [Performance](../infrastructure/performance.md) for retry pattern details.
