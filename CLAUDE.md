# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CyclingAssistant is an Android application built with Jetpack Compose following modern Android development practices. The app helps cyclists discover new routes by suggesting random cycling destinations within a specified distance range. It uses single-activity architecture with Material Design 3 theming, Compose Navigation for screen routing, and implements Clean Architecture with three distinct layers (Presentation, Domain, Data).

## Build Commands

### Run Detekt (Lint & Auto-format)
```bash
./gradlew detektRun
```
This runs static code analysis with auto-correction enabled. The project enforces zero issues tolerance (`maxIssues: 0`).

**Important for Coding Agents:** Run detekt at most **2 times** to fix issues automatically. Any remaining issues after 2 runs should be left for manual resolution to avoid infinite loops.

### Build the App
```bash
./gradlew build
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Install on Device/Emulator
```bash
./gradlew installDebug
```

### Clean Build
```bash
./gradlew clean build
```

## Architecture

### Multi-Module Structure

The project uses a multi-module architecture with feature modules and shared modules:

```
CyclingAssistant/
├── app/                        # Shell - hosts navigation, theme, Koin bootstrap
├── feature/
│   └── destinations/           # Destinations feature module (Clean Architecture)
├── shared/
│   ├── di/                     # Shared DI utilities (Koin qualifiers)
│   ├── concurrent/             # Shared coroutine dispatchers module
│   ├── graphics/               # Shared graphics utilities (bitmap creation)
│   └── location/               # Location services module
```

**Module Dependencies:**
```
app
├── feature:destinations
│   ├── shared:graphics
│   ├── shared:location
│   │   ├── shared:concurrent
│   │   │   └── shared:di
│   │   └── shared:di
│   ├── shared:concurrent
│   └── shared:di
├── shared:location
└── shared:concurrent
```

### Shared Modules

#### shared:di
Provides DI infrastructure for Koin qualifiers:
- `ClassNameQualifier` - Base qualifier class that uses class name as qualifier value

#### shared:concurrent
Provides injectable coroutine dispatchers via Koin:
```kotlin
// DispatchersQualifier sealed class with four variants
sealed class DispatchersQualifier : ClassNameQualifier() {
    data object Io : DispatchersQualifier()       // Dispatchers.IO
    data object Main : DispatchersQualifier()     // Dispatchers.Main
    data object Default : DispatchersQualifier()  // Dispatchers.Default
    data object Unconfined : DispatchersQualifier() // Dispatchers.Unconfined
}

// Usage in constructors:
get<CoroutineDispatcher>(DispatchersQualifier.Io)
```

#### shared:graphics
Provides graphics utilities for creating bitmaps:
- `createCircleBitmap()` - Creates a circle bitmap with fill and stroke colors

```kotlin
// Usage example
val bitmap = createCircleBitmap(
    sizeDp = 24,
    strokeWidthDp = 3,
    fillColor = Color.BLUE,
    strokeColor = Color.WHITE,
    density = resources.displayMetrics.density,
)
```

#### shared:location
Provides location services for all features:
- `Location` - Domain model for coordinates (latitude, longitude)
- `LocationDataSource` - Interface for location retrieval
- `LocationDataSourceImpl` - Google Play Services implementation
- `locationModule` - Koin module for DI

### Feature Modules

#### feature:destinations
The destinations feature implements Clean Architecture with three layers:

**Package:** `com.koflox.destinations`

```
feature/destinations/src/main/java/com/koflox/destinations/
├── di/
│   ├── DestinationsModule.kt    # Public - exports all DI (destinationsModule)
│   ├── DataModule.kt            # Internal - data layer DI
│   ├── DomainModule.kt          # Internal - domain layer DI
│   └── PresentationModule.kt    # Internal - presentation layer DI
├── domain/
│   ├── model/
│   │   ├── Destination.kt
│   │   └── Destinations.kt
│   ├── repository/
│   │   └── DestinationRepository.kt
│   ├── usecase/
│   │   ├── GetRandomDestinationUseCase.kt
│   │   ├── GetUserLocationUseCase.kt
│   │   └── InitializeDatabaseUseCase.kt
│   └── util/
│       └── DistanceCalculator.kt
├── data/
│   ├── mapper/
│   │   ├── DestinationMapper.kt
│   │   └── DestinationMapperImpl.kt
│   ├── repository/
│   │   └── DestinationRepositoryImpl.kt
│   └── source/
│       ├── asset/
│       │   ├── model/DestinationAsset.kt
│       │   ├── PoiAssetDataSource.kt
│       │   └── PoiAssetDataSourceImpl.kt
│       ├── local/
│       │   ├── database/
│       │   │   ├── AppDatabase.kt
│       │   │   └── dao/DestinationDao.kt
│       │   └── entity/DestinationLocal.kt
│       └── prefs/
│           ├── PreferencesDataSource.kt
│           └── PreferencesDataSourceImpl.kt
├── presentation/
│   ├── destinations/
│   │   ├── components/
│   │   │   ├── GoogleMapView.kt
│   │   │   ├── LetsGoButton.kt
│   │   │   ├── LoadingOverlay.kt
│   │   │   └── RouteSlider.kt
│   │   ├── model/
│   │   │   └── DestinationUiModel.kt
│   │   ├── DestinationsScreen.kt
│   │   ├── DestinationsUiEvent.kt
│   │   ├── DestinationsUiState.kt
│   │   └── DestinationsViewModel.kt
│   ├── mapper/
│   │   ├── DestinationUiMapper.kt
│   │   └── DestinationUiMapperImpl.kt
│   └── permission/
│       └── LocationPermissionHandler.kt
└── navigation/
    └── DestinationsNavigation.kt  # Navigation graph entry point
```

### App Module (Shell)

The app module serves as the application shell:

**Package:** `com.koflox.cyclingassistant`

```
app/src/main/java/com/koflox/cyclingassistant/
├── app/
│   ├── CyclingAssistantApplication.kt  # Koin DI initialization
│   └── Modules.kt                      # Root appModule
├── navigation/
│   ├── AppNavHost.kt                   # Main NavHost composable
│   └── NavRoutes.kt                    # Route constants
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt                     # Single activity entry point
```

### Navigation

The app uses Jetpack Compose Navigation for screen routing:

**NavHost Setup (AppNavHost.kt):**
```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.DESTINATIONS,
) {
    NavHost(navController, startDestination) {
        destinationsScreen()  // From feature:destinations
    }
}
```

**Feature Navigation Entry Point (DestinationsNavigation.kt):**
```kotlin
const val DESTINATIONS_ROUTE = "destinations"

fun NavGraphBuilder.destinationsScreen() {
    composable(route = DESTINATIONS_ROUTE) {
        DestinationsScreen()
    }
}
```

### Clean Architecture Layers (in feature modules)

#### 1. Presentation Layer (MVVM Pattern)
- **Composables**: Declarative UI with Jetpack Compose and Material3
- **ViewModels**: AndroidViewModel with Application for string resources
- **UI State**: Data classes with properties for state management
- **UI Events**: Sealed interfaces for user interactions
- **Mappers**: Convert domain models to UI models

#### 2. Domain Layer (Business Logic)
- **Use Cases**: Encapsulate single business operations (interface + implementation)
- **Domain Models**: Pure Kotlin data classes (no Android dependencies)
- **Repository Interfaces**: Contracts for data access
- **Utilities**: Helper classes (e.g., DistanceCalculator)

#### 3. Data Layer (Implementation Details)
- **Repository Implementations**: Concrete implementations of domain repositories
- **Data Sources**: Multiple sources (Room, Assets, SharedPreferences)
- **Local Database**: Room for persistent storage
- **Mappers**: Convert between data models (Asset ↔ Entity ↔ Domain)

### Dependency Injection (Koin)

**Module Hierarchy:**
```
appModule (app/Modules.kt)
├── concurrentModule (shared:concurrent)
│   ├── DispatchersQualifier.Io -> Dispatchers.IO
│   ├── DispatchersQualifier.Main -> Dispatchers.Main
│   ├── DispatchersQualifier.Default -> Dispatchers.Default
│   └── DispatchersQualifier.Unconfined -> Dispatchers.Unconfined
│
├── locationModule (shared:location)
│   └── LocationDataSource (single)
│
└── destinationsModule (feature:destinations)
    ├── domainModule
    │   ├── GetRandomDestinationUseCase (factory)
    │   ├── GetUserLocationUseCase (factory)
    │   ├── InitializeDatabaseUseCase (factory)
    │   └── DistanceCalculator (single)
    │
    ├── presentationModule
    │   ├── DestinationsViewModel (viewModelOf)
    │   └── DestinationUiMapper (single)
    │
    └── dataModules
        ├── dataModule
        │   └── DestinationMapper (single)
        ├── dataSourceModule
        │   ├── AppDatabase (single)
        │   ├── DestinationDao (single)
        │   ├── PoiAssetDataSource (single)
        │   └── PreferencesDataSource (single)
        └── repoModule
            └── DestinationRepository (single)
```

**DI Initialization:** `CyclingAssistantApplication.kt` initializes Koin in `onCreate()` with `startKoin()` loading `appModule`

### Data Persistence (Room)

- **Database**: `AppDatabase` in feature:destinations
- **Database Name**: `destinations_db`
- **Schema Export**: `schemas/destinations/` (project root)
- **Entity**: `DestinationLocal` table for cycling POI data
- **DAO**: `DestinationDao` with `getAllDestinations()` and `insertAll()`

**First Launch Initialization:**
1. `InitializeDatabaseUseCase` runs on app startup
2. Checks SharedPreferences flag `database_initialized`
3. If first launch, reads `assets/destinations.json` from feature module
4. Parses JSON using Kotlinx Serialization
5. Converts to Room entities and bulk inserts
6. Sets initialization flag

### Location Services

- **Module**: `shared:location`
- **Provider**: Google Play Services Location 21.3.0
- **Implementation**: `LocationDataSourceImpl`
- **Permission Handling**: `LocationPermissionHandler` in feature:destinations
- **Permission Required**: `ACCESS_FINE_LOCATION`

### Google Maps Integration

- **Library**: Google Maps Compose 7.0.0
- **Component**: `GoogleMapView.kt` in feature:destinations
- **API Key**: Must be configured in `secrets.properties` (see API Keys section)
- **Features**: Displays user location, destination markers, camera animation

## Code Quality & Standards

### Detekt Configuration
- **Config Location**: `.lint/detekt/detekt-config.yml`
- **Auto-correction**: Enabled by default on `./gradlew detektRun`
- **Enforcement**: Fails build if any issues found (`maxIssues: 0`)
- **Rules**: 900+ rules including complexity, formatting, naming, and potential bugs
- **Agent Limit**: Run at most 2 times automatically; leave remaining issues for manual fix

### Key Code Conventions

#### Naming
- **Classes**: PascalCase (`[A-Z][a-zA-Z0-9]*`)
- **Functions**: camelCase (`[a-z][a-zA-Z0-9]*`)
- **Composables**: Exempt from camelCase rule (use PascalCase)
- **Boolean Properties**: Must start with `is`, `has`, or `are`
- **Constants**: UPPER_SNAKE_CASE

#### Formatting
- **Max Line Length**: 150 characters
- **Indentation**: 4 spaces
- **Trailing Commas**: Required on declaration sites
- **Final Newline**: Required in all files

#### Complexity Limits
- **Method Complexity**: Max 15 (McCabe)
- **Method Length**: Max 60 lines
- **Class Size**: Max 600 lines
- **Nested Depth**: Max 4 levels
- **Return Statements**: Max 2 per function

#### Coroutines
- Avoid `GlobalScope` usage
- Inject dispatchers via `DispatchersQualifier` rather than hardcoding
- Don't use `Thread.sleep()` in suspend functions - use `delay()`

## Technology Stack

### Core Dependencies
- **Kotlin**: 2.3.0 (Java 11 target)
- **Android Gradle Plugin**: 8.12.3
- **KSP**: 2.3.4 (Kotlin Symbol Processing for Room)
- **Compose BOM**: 2025.12.01
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)

### Key Libraries

#### Architecture & DI
- **Koin**: 4.1.1 (Dependency Injection)
- **Lifecycle/ViewModel**: 2.10.0
- **Navigation Compose**: 2.9.6
- **Coroutines**: 1.10.2

#### Data & Persistence
- **Room**: 2.8.4 (Local SQLite database)
- **Kotlinx Serialization**: 1.9.0 (JSON parsing)

#### UI Framework
- **Jetpack Compose** (via BOM 2025.12.01)
- **Material3** (Material Design 3)
- **Activity Compose**: 1.12.2

#### Maps & Location
- **Google Maps Compose**: 7.0.0
- **Google Play Services Location**: 21.3.0
- **Accompanist Permissions**: 0.37.3

#### Testing
- **JUnit**: 4.13.2
- **MockK**: 1.14.7
- **Turbine**: 1.2.1
- **Coroutines Test**: 1.10.2

## Development Guidelines

### Adding a New Feature Module

1. **Create module structure:**
   ```
   feature/<feature-name>/
   ├── build.gradle.kts
   └── src/main/java/com/koflox/<feature-name>/
       ├── di/
       ├── domain/
       ├── data/
       ├── presentation/
       └── navigation/
   ```

2. **Add to settings.gradle.kts:**
   ```kotlin
   include(":feature:<feature-name>")
   ```

3. **Create navigation entry point:**
   ```kotlin
   // navigation/<FeatureName>Navigation.kt
   const val FEATURE_ROUTE = "feature_name"

   fun NavGraphBuilder.featureScreen() {
       composable(route = FEATURE_ROUTE) {
           FeatureScreen()
       }
   }
   ```

4. **Export DI module:**
   ```kotlin
   // di/<FeatureName>Module.kt
   val featureModule = module {
       includes(domainModule, presentationModule)
       includes(dataModules)
   }
   ```

5. **Register in app module:**
   - Add dependency in `app/build.gradle.kts`
   - Import module in `app/Modules.kt`
   - Add navigation in `AppNavHost.kt`

### Clean Architecture Patterns

#### Adding a New Use Case
```kotlin
// 1. Domain layer - interface
interface GetSomethingUseCase {
    suspend fun execute(): Result<Something>
}

// 2. Domain layer - implementation
class GetSomethingUseCaseImpl(
    private val repository: SomeRepository,
) : GetSomethingUseCase {
    override suspend fun execute(): Result<Something> = repository.getSomething()
}

// 3. DI registration
factory<GetSomethingUseCase> { GetSomethingUseCaseImpl(get()) }
```

#### Adding a New Data Source
1. Create interface in `data/source/`
2. Create implementation
3. Register in `DataModule.kt` (dataSourceModule)
4. Update repository to use new data source

### Room Database

#### Modifying Database Schema
1. Update entity class with new fields/tables
2. Increment version number in `@Database` annotation
3. Provide migration in `AppDatabase.kt`
4. Add migration to Room database builder in `DataModule.kt`
5. Schemas auto-generated in `schemas/<module-name>/`

### State Management (MVVM)

```kotlin
class FeatureViewModel(
    private val useCase: UseCaseType,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    fun onEvent(event: FeatureUiEvent) {
        when (event) {
            is FeatureUiEvent.Action -> handleAction()
        }
    }
}
```

## API Keys & Security

### Google Maps API Key Setup
1. Get an API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Enable "Maps SDK for Android" for your project
3. Copy `secrets.properties.example` to `secrets.properties`
4. Add your key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_actual_api_key_here
   ```
5. Never commit `secrets.properties` to version control (already in .gitignore)

## File Locations

### Key Configuration Files
- **Root Build**: `build.gradle.kts` - Detekt setup, subprojects config
- **App Build**: `app/build.gradle.kts` - App configuration
- **Feature Build**: `feature/destinations/build.gradle.kts` - Feature module config
- **Version Catalog**: `gradle/libs.versions.toml` - Centralized dependency versions
- **Settings**: `settings.gradle.kts` - Module includes
- **Detekt Config**: `.lint/detekt/detekt-config.yml` - Code quality rules

### Source Code Structure
- **App Module**: `app/src/main/java/com/koflox/cyclingassistant/` - Shell, navigation, theme
- **Feature Module**: `feature/destinations/src/main/java/com/koflox/destinations/` - Full feature
- **Shared Concurrent**: `shared/concurrent/src/main/java/com/koflox/concurrent/` - Dispatchers
- **Shared DI**: `shared/di/src/main/java/com/koflox/di/` - DI qualifiers
- **Shared Graphics**: `shared/graphics/src/main/java/com/koflox/graphics/` - Bitmap utilities
- **Shared Location**: `shared/location/src/main/java/com/koflox/location/` - Location services

### Database Schemas
- **Destinations**: `schemas/destinations/` - Room database JSON schemas (project root)

### Resources & Assets
- **Feature Assets**: `feature/destinations/src/main/assets/` - destinations.json
- **Feature Strings**: `feature/destinations/src/main/res/values/strings.xml`
- **App Theme**: `app/src/main/java/com/koflox/cyclingassistant/ui/theme/`
