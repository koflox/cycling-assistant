# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CyclingAssistant is an Android application built with Jetpack Compose following modern Android development practices. The app helps cyclists discover new routes by suggesting random cycling destinations within a specified distance range. It uses single-activity architecture with Material Design 3 theming and implements Clean Architecture with three distinct layers (Presentation, Domain, Data).

**Future Modularization Note:** The destinations feature in the app module is planned for extraction into a separate feature module. When adding new features, follow the existing modular patterns to facilitate this migration.

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

The project uses a multi-module architecture:

```
CyclingAssistant/
├── app/                    # Main application module (destinations feature)
├── shared/
│   ├── di/                 # Shared DI utilities (Koin qualifiers)
│   └── concurrent/         # Shared coroutine dispatchers module
```

**Module Dependencies:**
- `app` depends on `shared:di` and `shared:concurrent`
- `shared:concurrent` depends on `shared:di`
- `shared:di` has no internal dependencies (only `koin-core`)

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

### Clean Architecture with MVVM
The app module implements **Clean Architecture** with clear separation of concerns across three layers:

#### 1. Presentation Layer (MVVM Pattern)
- **Composables**: Declarative UI with Jetpack Compose and Material3
- **ViewModels**: Manage UI state with StateFlow for reactive updates
- **UI State**: Data classes with properties for state management
- **UI Events**: Sealed interfaces for user interactions
- **Mappers**: Convert domain models to UI models
- **Location**: `presentation/` package

**Key Components:**
- `DestinationsScreen.kt` - Primary UI screen
- `DestinationsViewModel.kt` - State management and business logic coordination
- `DestinationsUiState.kt` - UI state data class (isLoading, selectedDestination, userLocation, etc.)
- `DestinationsUiEvent.kt` - UI events (RouteDistanceChanged, LetsGoClicked, PermissionGranted, etc.)
- `presentation/destinations/components/` - Reusable composables (GoogleMapView, RouteSlider, etc.)
- `LocationPermissionHandler.kt` - Manages location permission requests

#### 2. Domain Layer (Business Logic)
- **Use Cases**: Encapsulate single business operations (interface + implementation pattern)
- **Domain Models**: Pure Kotlin data classes (no Android dependencies)
- **Repository Interfaces**: Contracts for data access
- **Utilities**: Helper classes (e.g., DistanceCalculator using Haversine formula)
- **Location**: `domain/` package

**Key Components:**
- `GetRandomDestinationUseCase` - Interface + `GetRandomDestinationUseCaseImpl` - Finds destinations within distance range
- `GetUserLocationUseCase` - Interface + `GetUserLocationUseCaseImpl` - Retrieves current user location
- `InitializeDatabaseUseCase` - Interface + `InitializeDatabaseUseCaseImpl` - Seeds database from assets on first launch
- `DestinationRepository` - Interface for destination data access
- `Destination.kt`, `Location.kt` - Domain models
- `DistanceCalculator.kt` - Haversine formula implementation

#### 3. Data Layer (Implementation Details)
- **Repository Implementations**: Concrete implementations of domain repositories
- **Data Sources**: Multiple sources (Room, Assets, Location, SharedPreferences)
- **Local Database**: Room for persistent storage
- **Mappers**: Convert between data models (Asset ↔ Entity ↔ Domain)
- **Location**: `data/` package

**Data Sources:**
- `PoiAssetDataSource` - Reads destinations from `assets/destinations.json`
- `AppDatabase` + `DestinationDao` - Room database for local storage
- `LocationDataSource` - Google Play Services for GPS location
- `PreferencesDataSource` - SharedPreferences for app state

### Current Package Structure
```
com.koflox.cyclingassistant/
├── app/
│   └── CyclingAssistantApplication.kt  # Koin DI initialization
│   └── Modules.kt                      # Root appModule
├── MainActivity.kt                     # Single activity entry point
│
├── presentation/                       # Presentation Layer (MVVM)
│   ├── destinations/
│   │   ├── components/
│   │   │   ├── GoogleMapView.kt
│   │   │   ├── LetsGoButton.kt
│   │   │   ├── LoadingOverlay.kt
│   │   │   └── RouteSlider.kt
│   │   ├── model/
│   │   │   ├── DestinationUiModel.kt
│   │   │   └── DestinationsUiModel.kt
│   │   ├── DestinationsScreen.kt
│   │   ├── DestinationsUiEvent.kt
│   │   ├── DestinationsUiState.kt
│   │   └── DestinationsViewModel.kt
│   ├── mapper/
│   │   ├── DestinationUiMapper.kt
│   │   └── DestinationUiMapperImpl.kt
│   ├── permission/
│   │   └── LocationPermissionHandler.kt
│   └── ui/theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── domain/                             # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── Destination.kt
│   │   ├── Destinations.kt
│   │   └── Location.kt
│   ├── repository/
│   │   └── DestinationRepository.kt
│   ├── usecase/
│   │   ├── GetRandomDestinationUseCase.kt      # Interface + Impl
│   │   ├── GetUserLocationUseCase.kt           # Interface + Impl
│   │   └── InitializeDatabaseUseCase.kt        # Interface + Impl
│   └── util/
│       └── DistanceCalculator.kt
│
├── data/                               # Data Layer (Implementation)
│   ├── mapper/
│   │   ├── DestinationMapper.kt
│   │   └── DestinationMapperImpl.kt
│   ├── prefs/
│   │   ├── PreferencesDataSource.kt
│   │   └── PreferencesDataSourceImpl.kt
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
│       └── location/
│           ├── LocationDataSource.kt
│           └── LocationDataSourceImpl.kt
│
└── di/                                 # Dependency Injection (Koin)
    ├── DestinationsModule.kt           # Feature module aggregator
    ├── DataModule.kt                   # Data layer modules
    ├── DomainModule.kt                 # Domain layer module
    └── PresentationModule.kt           # Presentation layer module
```

### Dependency Injection (Koin)

The project uses **Koin 4.1.1** for dependency injection with modular configuration:

**Module Hierarchy:**
```
appModule (app/Modules.kt)
├── concurrentModule (shared:concurrent)
│   ├── DispatchersQualifier.Io -> Dispatchers.IO
│   ├── DispatchersQualifier.Main -> Dispatchers.Main
│   ├── DispatchersQualifier.Default -> Dispatchers.Default
│   └── DispatchersQualifier.Unconfined -> Dispatchers.Unconfined
│
└── destinationModule (di/DestinationsModule.kt)
    ├── domainModule (di/DomainModule.kt)
    │   ├── GetRandomDestinationUseCase (factory)
    │   ├── GetUserLocationUseCase (factory)
    │   ├── InitializeDatabaseUseCase (factory)
    │   └── DistanceCalculator (single)
    │
    ├── presentationModule (di/PresentationModule.kt)
    │   ├── DestinationsViewModel (viewModelOf)
    │   └── DestinationUiMapper (single)
    │
    └── dataModules (di/DataModule.kt) - List of 3 modules
        ├── dataModule
        │   └── DestinationMapper (single)
        ├── dataSourceModule
        │   ├── AppDatabase (single)
        │   ├── DestinationDao (single)
        │   ├── PoiAssetDataSource (single)
        │   ├── LocationDataSource (single)
        │   └── PreferencesDataSource (single)
        └── repoModule
            └── DestinationRepository (single)
```

**DI Initialization:** `CyclingAssistantApplication.kt` initializes Koin in `onCreate()` with `startKoin()` loading `appModule`

**Dispatcher Injection Pattern:**
```kotlin
// In module definition
factory<CoroutineDispatcher>(DispatchersQualifier.Io) { Dispatchers.IO }

// In constructors - get qualified dispatcher
get<CoroutineDispatcher>(DispatchersQualifier.Io)
```

**ViewModel Injection:** Use `viewModelOf()` in Koin modules for ViewModel creation

### Data Persistence (Room)
- **Database**: `AppDatabase` with version 1
- **Database Name**: `cycling_assistant_db`
- **Schema Export**: Enabled - schemas exported to `schemas/app/` (project root)
- **Entity**: `DestinationLocal` table for cycling POI data
- **DAO**: `DestinationDao` with `getAllDestinations()` and `insertAll()`

**First Launch Initialization:**
1. `InitializeDatabaseUseCase` runs on app startup
2. Checks SharedPreferences flag `database_initialized`
3. If first launch, reads `assets/destinations.json` (Tokyo cycling destinations)
4. Parses JSON using Kotlinx Serialization
5. Converts to Room entities and bulk inserts
6. Sets initialization flag

### Location Services
- **Provider**: Google Play Services Location 21.3.0
- **Implementation**: `LocationDataSourceImpl` in data layer
- **Permission Handling**: `LocationPermissionHandler` in presentation layer with Accompanist Permissions
- **Permission Required**: `ACCESS_FINE_LOCATION`

### Google Maps Integration
- **Library**: Google Maps Compose 7.0.0
- **Component**: `GoogleMapView.kt` in presentation layer
- **API Key**: Must be configured in `secrets.properties` (see API Keys section)
- **Features**: Displays user location, destination markers, camera animation to bounds

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
- **Import Order**: `*`, `java.**`, `javax.**`, `kotlin.**`, custom packages
- **Trailing Commas**: Required on declaration sites
- **Final Newline**: Required in all files

#### Complexity Limits
- **Method Complexity**: Max 15 (McCabe)
- **Method Length**: Max 60 lines
- **Class Size**: Max 600 lines
- **Nested Depth**: Max 4 levels
- **Nested Scope Functions**: Max 2 levels
- **Return Statements**: Max 2 per function

#### Coroutines
- Avoid `GlobalScope` usage
- Inject dispatchers via `DispatchersQualifier` rather than hardcoding
- Don't use `Thread.sleep()` in suspend functions - use `delay()`

### Testing
- **Unit Tests**: JUnit 4 in `app/src/test/`
- **Instrumented Tests**: Android JUnit + Espresso in `app/src/androidTest/`
- **Compose Tests**: Use `androidx.compose.ui.test` for UI testing

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
  - `io.insert-koin:koin-android` - Android Koin support
  - `io.insert-koin:koin-androidx-compose` - Compose integration
- **Lifecycle/ViewModel**: 2.10.0
  - `androidx.lifecycle:lifecycle-runtime-ktx`
  - `androidx.lifecycle:lifecycle-viewmodel-compose`
- **Coroutines**: 1.10.2 (Asynchronous programming)

#### Data & Persistence
- **Room**: 2.8.4 (Local SQLite database)
  - `androidx.room:room-runtime`
  - `androidx.room:room-ktx` (Coroutines support)
  - `androidx.room:room-compiler` (KSP)
- **Kotlinx Serialization**: 1.9.0 (JSON parsing)
  - `org.jetbrains.kotlinx:kotlinx-serialization-json`

#### UI Framework
- **Jetpack Compose** (via BOM 2025.12.01)
  - `androidx.compose.ui`
  - `androidx.compose.material3` (Material Design 3)
  - `androidx.compose.ui.tooling` (Preview support)
- **Activity Compose**: 1.12.2
- **Core KTX**: 1.16.0

#### Maps & Location
- **Google Maps Compose**: 7.0.0
  - `com.google.maps.android:maps-compose`
  - `com.google.maps.android:maps-compose-utils`
- **Google Play Services**:
  - `com.google.android.gms:play-services-location` (21.3.0)
  - `com.google.android.gms:play-services-maps` (19.2.0)
- **Accompanist Permissions**: 0.37.3 (Permission handling for Compose)

#### Testing
- **JUnit**: 4.13.2 (Unit testing framework)
- **MockK**: 1.14.7 (Kotlin mocking library)
- **Turbine**: 1.2.1 (Flow testing utilities)
- **Coroutines Test**: 1.10.2 (Testing coroutines)
- **Koin Test**: 4.1.1 (DI testing)
- **Room Testing**: 2.8.4
- **AndroidX Test**: JUnit 1.2.1, Espresso 3.7.0
- **Compose UI Test**: JUnit4 + Test Manifest

### Build System
- **Version Catalog**: Centralized dependency management in `gradle/libs.versions.toml`
- **Gradle**: 8.12.3
- **Detekt**: 1.23.8 (formatting + analysis)
- **Secrets Gradle Plugin**: 2.0.1 (API key management)

## Development Guidelines

### When Adding Features
1. **Follow Clean Architecture**: Place code in the correct layer (presentation/domain/data)
2. **Single Activity Pattern**: Add Composables and screens, not new Activities
3. **Use Material3 Components**: Import from `androidx.compose.material3`
4. **Respect Layer Boundaries**:
   - Presentation depends on Domain only (never Data)
   - Domain has no dependencies on other layers
   - Data depends on Domain for repository interfaces
5. **Use Dependency Injection**: Register new dependencies in appropriate Koin module
6. **Run Detekt**: Execute `./gradlew detektRun` before committing (max 2 auto-runs for agents)

### Clean Architecture Patterns

#### Adding a New Feature (e.g., "Save Favorite Destinations")
1. **Domain Layer First**:
   - Create domain model: `data class FavoriteDestination(...)`
   - Create repository interface: `interface FavoriteRepository`
   - Create use case interface + impl: `GetFavoritesUseCase` / `GetFavoritesUseCaseImpl`
   - Register use case in `DomainModule.kt`

2. **Data Layer**:
   - Create Room entity: `@Entity data class FavoriteDestinationLocal`
   - Create DAO: `@Dao interface FavoriteDestinationDao`
   - Update `AppDatabase` to include new DAO
   - Increment database version and provide migration
   - Create mapper: `FavoriteDestinationMapper` (Entity ↔ Domain)
   - Implement repository: `class FavoriteRepositoryImpl : FavoriteRepository`
   - Register in `DataModule.kt`

3. **Presentation Layer**:
   - Create UI model: `data class FavoriteDestinationUiModel`
   - Create UI mapper: `FavoriteDestinationUiMapper` (Domain ↔ UI)
   - Update `DestinationsUiState` or create new screen state
   - Update `DestinationsUiEvent` or create new screen events
   - Update ViewModel to handle new use case
   - Create/update Composables for UI
   - Register mapper in `PresentationModule.kt`

#### Adding a New Data Source (e.g., "Remote API")
1. Create interface in `data/source/remote/`
2. Implement using Retrofit/Ktor
3. Create data models for API responses
4. Create mapper from API model to domain model
5. Register in `DataModule.kt` (dataSourceModule)
6. Update repository implementation to use new data source

### Dependency Injection (Koin)

#### ViewModel Injection
```kotlin
// In PresentationModule.kt
viewModelOf(::DestinationsViewModel)

// In Composable
val viewModel = koinViewModel<DestinationsViewModel>()
```

#### Use Case Injection
```kotlin
// In DomainModule.kt - interface binding with implementation
factory<GetRandomDestinationUseCase> {
    GetRandomDestinationUseCaseImpl(
        destinationRepository = get(),
        distanceCalculator = get(),
    )
}

// In ViewModel constructor
class DestinationsViewModel(
    private val getRandomDestination: GetRandomDestinationUseCase
)
```

#### Repository Injection
```kotlin
// In DataModule.kt (repoModule)
single<DestinationRepository> {
    DestinationRepositoryImpl(
        dispatcher = get(DispatchersQualifier.Io),
        destinationDao = get(),
        // ... other dependencies
    )
}
```

#### Dispatcher Injection
```kotlin
// In shared:concurrent module
factory<CoroutineDispatcher>(DispatchersQualifier.Io) { Dispatchers.IO }

// Usage in data sources/repositories
class LocationDataSourceImpl(
    private val dispatcher: CoroutineDispatcher,  // Injected with qualifier
) : LocationDataSource
```

### Room Database

#### Modifying Database Schema
1. Update entity class with new fields/tables
2. Increment version number in `@Database` annotation
3. Provide migration in `AppDatabase.kt`:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(db: SupportSQLiteDatabase) {
           db.execSQL("ALTER TABLE destinations ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
       }
   }
   ```
4. Add migration to Room database builder in `DataModule.kt`
5. Update schema export: schemas will be auto-generated in `schemas/app/`

#### Testing Room
- Use in-memory database for unit tests
- Use `allowMainThreadQueries()` for tests only

### State Management (MVVM)

#### ViewModel Pattern
```kotlin
class FeatureViewModel(
    private val useCase: UseCaseType
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    fun onEvent(event: FeatureUiEvent) {
        when (event) {
            is FeatureUiEvent.Action -> handleAction()
        }
    }
}
```

#### State Collection in Composables
```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingOverlay()
    }
    // Render based on state properties
}
```

### Location & Permissions

#### Requesting Location Permission
Use `LocationPermissionHandler` composable:
```kotlin
LocationPermissionHandler(
    onPermissionGranted = { viewModel.onEvent(DestinationsUiEvent.PermissionGranted) },
    onPermissionDenied = { viewModel.onEvent(DestinationsUiEvent.PermissionDenied) }
) {
    // Content that requires location permission
}
```

#### Getting User Location
Inject `GetUserLocationUseCase` in ViewModel and call within coroutine:
```kotlin
viewModelScope.launch {
    getUserLocation.getLocation()
        .onSuccess { location -> /* use location */ }
        .onFailure { error -> /* handle error */ }
}
```

### Google Maps Integration

#### API Key Configuration
1. Obtain API key from Google Cloud Console
2. Enable "Maps SDK for Android"
3. Add to `secrets.properties`: `MAPS_API_KEY=your_key_here`
4. Key is automatically injected via Secrets Gradle Plugin

#### Using Maps in Composables
```kotlin
GoogleMapView(
    userLocation = uiState.userLocation,
    destinations = uiState.destinations,
    onDestinationClick = { /* handle click */ },
    modifier = Modifier.fillMaxSize()
)
```

### Composable Function Rules
- **Naming**: Use PascalCase (exempt from Detekt camelCase rule)
- **Size**: Keep Composables focused and small (<60 lines)
- **Reusability**: Extract common UI patterns to `presentation/*/components/`
- **State**: Use `remember` for local UI state, ViewModel + StateFlow for screen state
- **Side Effects**: Use `LaunchedEffect`, `DisposableEffect` appropriately
- **Preview**: Add `@Preview` annotations for Android Studio preview

### Theme System
- **Colors**: Edit `presentation/ui/theme/Color.kt` for custom color schemes
- **Typography**: Edit `presentation/ui/theme/Type.kt` for text styles
- **Theme**: Edit `presentation/ui/theme/Theme.kt` for Material theme configuration
- **Dynamic Color**: Enabled by default on Android 12+ (Material You)

### Edge-to-Edge UI
The app uses `enableEdgeToEdge()` in MainActivity:
- Be aware of system bars (status bar, navigation bar)
- Use `WindowInsets` APIs for proper padding
- Test on devices with different screen sizes, notches, and cutouts
- Use `Modifier.systemBarsPadding()` or specific inset padding as needed

### Testing Guidelines

#### Unit Tests (JUnit + MockK)
- Test use cases with mocked repositories
- Test ViewModels with mocked use cases and Turbine for Flow testing
- Test mappers with real data transformations
- Use `runTest` for coroutine testing

#### Instrumented Tests
- Test Room DAOs with in-memory database
- Test Composables with `createComposeRule()`
- Test navigation flows
- Test permission handling

#### Test Organization
- Unit tests: `app/src/test/java/.../`
- Instrumented tests: `app/src/androidTest/java/.../`
- Mirror package structure of main source

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
6. `secrets.properties` is dedicated for secrets and won't be auto-generated

## File Locations

### Key Configuration Files
- **Root Build**: `build.gradle.kts` - Detekt setup
- **App Build**: `app/build.gradle.kts` - App configuration, dependencies, KSP, Secrets plugin
- **Shared DI Build**: `shared/di/build.gradle.kts` - DI utilities module
- **Shared Concurrent Build**: `shared/concurrent/build.gradle.kts` - Dispatchers module
- **Version Catalog**: `gradle/libs.versions.toml` - Centralized dependency versions
- **Settings**: `settings.gradle.kts` - Project settings, includes `:app`, `:shared:di`, `:shared:concurrent`
- **Detekt Config**: `.lint/detekt/detekt-config.yml` - Code quality rules (900+ rules)
- **Secrets**: `secrets.properties` - API keys and secrets (NOT in git, create from .example)
- **Secrets Example**: `secrets.properties.example` - Template for required API keys
- **AndroidManifest**: `app/src/main/AndroidManifest.xml` - App configuration, permissions

### Source Code Structure
- **App Module**: `app/src/main/java/com/koflox/cyclingassistant/`
  - **Application**: `app/CyclingAssistantApplication.kt` - Koin initialization
  - **Root Module**: `app/Modules.kt` - appModule definition
  - **MainActivity**: `MainActivity.kt` - Single activity entry point
  - **Presentation**: `presentation/` - UI layer (Composables, ViewModels, UI state/events, theme)
  - **Domain**: `domain/` - Business logic (Use cases, domain models, repository interfaces)
  - **Data**: `data/` - Data layer (Repositories, data sources, Room, mappers)
  - **DI**: `di/` - Feature-specific Koin modules (DestinationsModule, DataModule, DomainModule, PresentationModule)
- **Shared DI Module**: `shared/di/src/main/java/com/koflox/di/`
  - `ClassNameQualifier.kt` - Base qualifier class for Koin
- **Shared Concurrent Module**: `shared/concurrent/src/main/java/com/koflox/concurrent/`
  - `Modules.kt` - concurrentModule with DispatchersQualifier
- **Unit Tests**: `app/src/test/java/com/koflox/cyclingassistant/`
- **Instrumented Tests**: `app/src/androidTest/java/com/koflox/cyclingassistant/`

### Resources & Assets
- **Assets**: `app/src/main/assets/` - Static files (e.g., `destinations.json`)
- **Resources**: `app/src/main/res/` - Android resources (layouts, drawables, strings)
- **Strings**: `app/src/main/res/values/strings.xml` - Localized strings

### Database
- **Schema Export**: `schemas/app/` - Room database JSON schemas at project root (version controlled)
- **Database File**: `data/databases/cycling_assistant_db` (on device, not in repo)

### Dependencies & Build
- **Gradle Wrapper**: `gradle/wrapper/` - Gradle 8.12.3
- **Gradle Properties**: `gradle.properties` - Build configuration
- **Settings**: `settings.gradle.kts` - Project settings, version catalog, module includes
