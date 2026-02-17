# Tech Stack

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
| **CI**                   | GitHub Actions               |
| **Dependencies Update**  | Dependabot                   |

## Database

Room is used as the local database, centralized in the `app` module (`AppDatabase`):

- **DestinationDao** — cycling POI data
- **SessionDao** — session and track points

DAO conventions: `@Dao` interfaces with suspend functions for one-shot operations and `Flow` for observable queries.

## Data Persistence

| Storage   | Purpose                                     |
|-----------|---------------------------------------------|
| Room      | Destinations, sessions, and track points    |
| DataStore | User preferences (theme, language, profile) |
