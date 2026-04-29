# Tech Stack

| Category                 | Technology                   |
|--------------------------|------------------------------|
| **Language**             | Kotlin                       |
| **UI Framework**         | Jetpack Compose (Material 3) |
| **Architecture**         | MVVM + Clean Architecture    |
| **Dependency Injection** | Hilt                         |
| **Async**                | Kotlin Coroutines & Flow     |
| **Database**             | Room                         |
| **Preferences**          | DataStore                    |
| **Navigation**           | Navigation Compose           |
| **Maps**                 | Google Maps Compose          |
| **Location**             | Play Services Location       |
| **Bluetooth**            | Android BLE (BluetoothGatt, BluetoothLeScanner) |
| **Networking**           | Ktor Client (OkHttp engine), kotlinx.serialization |
| **External APIs**        | Strava REST v3 (OAuth 2.0, GPX uploads) |
| **Testing**              | JUnit 4, MockK, Turbine, Roborazzi, Robolectric |
| **Observability**        | Firebase Crashlytics, Firebase Performance, Timber |
| **Performance**          | Baseline Profiles, Macrobenchmarks |
| **Code Quality**         | Detekt                       |
| **Coverage**             | Kover                        |
| **Build**                | Gradle, AGP, R8              |
| **Min SDK**              | 24 (Android 7.0)             |
| **Target SDK**           | 36                           |
| **CI**                   | GitHub Actions               |
| **Dependencies Update**  | Dependabot                   |

## Database

Room is used as the local database, centralized in the `app` module (`AppDatabase`):

- **DestinationDao** — cycling POI data
- **LocaleDao** — language/locale settings
- **PairedDeviceDao** — paired BLE devices
- **ProfileDao** — rider profile data
- **SessionDao** — session and track points

DAO conventions: `@Dao` interfaces with suspend functions for one-shot operations and `Flow` for observable queries.

## Data Persistence

| Storage   | Purpose                                                  |
|-----------|----------------------------------------------------------|
| Room      | Destinations, sessions, track points, locale, profile, paired devices |
| DataStore | User preferences (theme, language, profile, stats display configuration) |
