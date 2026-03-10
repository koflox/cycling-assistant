# Migration: Koin → Hilt

!!! info "Migration Date"
    March 2026 · Koin 4.1.1 → Hilt 2.56.2

## Motivation

| Concern | Koin | Hilt |
|---------|------|------|
| Graph validation | Runtime — errors surface during testing or in production | **Compile-time** — missing bindings fail the build |
| Android lifecycle | Manual `inject()` / `koinViewModel()` | First-party `@AndroidEntryPoint` with lifecycle-aware injection |
| KSP compatibility | N/A (runtime reflection) | KSP backend (already used for Room) |

The primary driver was **compile-time DI graph validation**. With ~30 modules and 52 DI files, runtime Koin errors were only caught during testing or in production. Hilt catches all missing bindings at build time.

## Strategy

**Big-bang migration** on a dedicated branch. Koin and Hilt cannot coexist because:

- Bridge modules wire dependencies across features — mixed containers break resolution
- `SessionTrackingService` uses `by inject()` — converting to `@AndroidEntryPoint` requires all transitive deps on Hilt
- `koinViewModel()` and `hiltViewModel()` draw from separate containers

## API Mapping

### Scope Mapping

| Koin | Hilt |
|------|------|
| `single { Foo() }` | `@Provides @Singleton fun provideFoo(): Foo` in `@InstallIn(SingletonComponent::class)` |
| `factory { Foo() }` | `@Provides fun provideFoo(): Foo` (no scope annotation) in `@InstallIn(SingletonComponent::class)` |
| `viewModel { FooViewModel(...) }` | `@HiltViewModel class FooViewModel @Inject constructor(...)` |

### Qualifier Mapping

Koin uses `ClassNameQualifier` sealed class hierarchies. Hilt uses flat `@Qualifier` annotations grouped by file:

| Koin Qualifier | Hilt Annotation | File |
|---|---|---|
| `DispatchersQualifier.Io` | `@IoDispatcher` | `DispatcherQualifiers.kt` |
| `DispatchersQualifier.Default` | `@DefaultDispatcher` | `DispatcherQualifiers.kt` |
| `DispatchersQualifier.Main` | `@MainDispatcher` | `DispatcherQualifiers.kt` |
| `DispatchersQualifier.Unconfined` | `@UnconfinedDispatcher` | `DispatcherQualifiers.kt` |
| `SessionQualifier.DaoFactory` | `@SessionDaoFactory` | `SessionQualifiers.kt` |
| `SessionQualifier.SessionMutex` | `@SessionMutex` | `SessionQualifiers.kt` |
| `ConnectionsQualifier.DaoFactory` | `@ConnectionsDaoFactory` | `ConnectionsQualifiers.kt` |
| `DestinationsQualifier.DaoFactory` | `@DestinationsDaoFactory` | `DestinationsQualifiers.kt` |
| `LocaleQualifier.DaoFactory` | `@LocaleDaoFactory` | `LocaleQualifiers.kt` |
| `ProfileQualifier.DaoFactory` | `@ProfileDaoFactory` | `ProfileQualifiers.kt` |
| `PresentationModuleQualifier.SessionErrorMessageMapper` | `@SessionErrorMapper` | `SessionQualifiers.kt` |
| `ConnectionsErrorMapperQualifier` | `@ConnectionsErrorMapper` | `ConnectionsQualifiers.kt` |
| `DestinationsDataQualifierInternal.DestinationFilesMutex` | `@DestinationFilesMutex` | `DestinationsQualifiers.kt` |

All qualifier annotations live in `shared/di` and follow the pattern:

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
```

!!! note "No hierarchy"
    Unlike Koin's `sealed class` hierarchy, Hilt qualifiers are flat annotations. Logical grouping is achieved through file organization (e.g., all dispatcher qualifiers in one file, all session qualifiers in another).

### Composable Injection Mapping

| Koin Pattern | Count | Hilt Replacement |
|---|---|---|
| `koinViewModel<VM>()` | 17 | `hiltViewModel<VM>()` |
| `koinInject<T>()` (non-VM) | 12 | `@EntryPoint` interface |
| `by viewModel()` (Activity) | 1 | `by viewModels()` |
| `by inject()` (Service) | 2 | `@AndroidEntryPoint` + `@Inject lateinit var` |

For `koinInject()` in Composables (bridge navigators, `BlePermissionChecker`, `CurrentTimeProvider`, etc.), create per-feature `@EntryPoint` interfaces:

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RideMapEntryPoint {
    fun sessionUiNavigator(): CyclingSessionUiNavigator
    fun nutritionUiNavigator(): NutritionUiNavigator
    fun poiUiNavigator(): PoiUiNavigator
}

// Usage in Composable
val context = LocalContext.current
val entryPoint = EntryPointAccessors.fromApplication(context, RideMapEntryPoint::class.java)
val navigator = entryPoint.sessionUiNavigator()
```

## Module File Structure

### Before (Koin)

```
feature/<name>/di/
    <Feature>Module.kt       # public aggregator: val featureModule = module { includes(...) }
    DataModule.kt            # private val dataModule, dataSourceModule, repoModule
    DomainModule.kt          # internal val domainModule
    PresentationModule.kt    # internal val presentationModule
```

### After (Hilt)

```
feature/<name>/di/
    <Feature>DataHiltModule.kt      # @Module @InstallIn(SingletonComponent::class) internal object
    <Feature>DomainHiltModule.kt    # @Module @InstallIn(SingletonComponent::class) internal object
    <Feature>PresentationHiltModule.kt  # only if non-VM bindings exist (error mappers, UI mappers)
```

No public aggregator needed — Hilt auto-discovers `@Module` classes via annotation processing.

### Example Conversion

=== "Koin"

    ```kotlin
    // DataModule.kt
    private val dataSourceModule = module {
        single<DestinationLocalDataSource> {
            DestinationLocalDataSourceImpl(
                daoFactory = get(DestinationsQualifier.DaoFactory),
                dispatcherIo = get(DispatchersQualifier.Io),
            )
        }
    }

    private val repoModule = module {
        single<DestinationRepository> {
            DestinationRepositoryImpl(
                localDataSource = get(),
            )
        }
    }

    internal val dataModules = listOf(dataSourceModule, repoModule)
    ```

=== "Hilt"

    ```kotlin
    // DestinationsDataHiltModule.kt
    @Module
    @InstallIn(SingletonComponent::class)
    internal object DestinationsDataHiltModule {

        @Provides
        @Singleton
        fun provideLocalDataSource(
            @DestinationsDaoFactory daoFactory: ConcurrentFactory<DestinationDao>,
            @IoDispatcher dispatcherIo: CoroutineDispatcher,
        ): DestinationLocalDataSource = DestinationLocalDataSourceImpl(
            daoFactory = daoFactory,
            dispatcherIo = dispatcherIo,
        )

        @Provides
        @Singleton
        fun provideRepository(
            localDataSource: DestinationLocalDataSource,
        ): DestinationRepository = DestinationRepositoryImpl(
            localDataSource = localDataSource,
        )
    }
    ```

## Visibility Rules

| Element | Koin Visibility | Hilt Visibility | Reason |
|---|---|---|---|
| ViewModels | `internal` | `public` | `@HiltViewModel @Inject constructor` requires public. Safe — VMs are never manually constructed. |
| `*Impl` classes | `internal` | `internal` (unchanged) | `@Provides` functions in same-module `@Module` objects can access `internal` classes. |
| `@Module` objects | N/A | `internal` | Auto-discovered by Hilt via KSP; no need for public visibility. |

## Android Component Changes

### Application

```kotlin
// Before
class CyclingAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CyclingAssistantApplication)
            modules(allModules)
        }
    }
}

// After
@HiltAndroidApp
class CyclingAssistantApplication : Application()
```

### Activity

```kotlin
// Before
class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
}

// After
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
}
```

### Foreground Service

```kotlin
// Before
class SessionTrackingService : Service(), KoinComponent {
    private val sessionTracker by inject<SessionTracker>()
    private val notificationBuilder by inject<SessionNotificationBuilder>()
}

// After
@AndroidEntryPoint
class SessionTrackingService : Service() {
    @Inject lateinit var sessionTracker: SessionTracker
    @Inject lateinit var notificationBuilder: SessionNotificationBuilder
}
```

## Database Module

The centralized Room database module moved from `app/Modules.kt` to `app/di/DatabaseHiltModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseHiltModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.build(context)

    @Provides
    @SessionDaoFactory
    @Singleton
    fun provideSessionDaoFactory(db: AppDatabase): ConcurrentFactory<SessionDao> =
        db.sessionDaoFactory()

    // ... other DAO factories with their respective qualifiers
}
```

## Verification

Hilt's compile-time validation replaces Koin's `DiVerificationTest`:

| Check | Koin | Hilt |
|-------|------|------|
| Missing bindings | Runtime test (`DiVerificationTest`) | **Build fails** — `./gradlew build` |
| Circular dependencies | Runtime crash | **Build fails** |
| Qualifier mismatches | Runtime crash | **Build fails** |
| Unit tests | Unchanged — MockK + manual construction | Unchanged |
| Integration tests | `startKoin { }` in test setup | `@HiltAndroidTest` + `HiltAndroidRule` |
