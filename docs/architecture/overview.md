# Architecture Overview

CyclingAssistant follows a **multi-module Clean Architecture** approach with MVVM pattern and unidirectional data flow. Features are isolated into independent modules that communicate through the [bridge pattern](bridge-pattern.md).

## Layer Dependencies

Each feature module follows strict unidirectional dependencies across three layers:

```
presentation → domain → data
```

- **presentation** — Composables, ViewModels, UI state
- **domain** — Use cases, repository interfaces, business logic (no Android dependencies)
- **data** — Repository implementations, data sources, mappers

```
Composable (Route/Content)
    │ observes
    ▼
ViewModel
    │ calls
    ▼
UseCase
    │ calls
    ▼
Repository
    │ calls
    ▼
DataSource
```

!!! warning "Dependency Rules"
    - Upper layers depend on lower layers, never the reverse
    - ViewModel never accesses Repository or DataSource directly — always through UseCase
    - DataSource never accesses UseCase or ViewModel

## Interface + Impl Pattern

UseCases, DataSources, Mappers, and Repositories all follow the Interface + Impl pattern:

- **UseCase**: interface and `Impl` are colocated in the same file, along with related custom exceptions
- **Mapper / DataSource**: interfaces may use separate files for interface and impl

## Error Handling

The project uses `suspendRunCatching` (from `shared/concurrent`) instead of Kotlin's standard `runCatching` in coroutine contexts. It rethrows `CancellationException` to preserve structured concurrency.

- Repository **suspend functions** return `Result<T>`
- Repository **observable functions** return `Flow<T>` (no `Result` wrapping)

## Dependency Injection (Hilt)

All dependencies are managed via [Hilt](https://dagger.dev/hilt/). Registration conventions:

| Type       | Hilt Pattern                                                    |
|------------|-----------------------------------------------------------------|
| UseCase    | `@Provides` (no scope) for stateless; `@Provides @Singleton` for stateful |
| DataSource | `@Provides @Singleton`                                          |
| Mapper     | `@Provides @Singleton`                                          |
| Repository | `@Provides @Singleton`                                          |
| ViewModel  | `@HiltViewModel internal class ... @Inject internal constructor(...)` |

### Qualifiers

Defined in `shared/di` as `@Qualifier @Retention(AnnotationRetention.BINARY)` annotations
(e.g., `@IoDispatcher`, `@DefaultDispatcher`, `@SessionMutex`, `@SessionDaoFactory`).

### DI File Organization

Each feature module organizes DI into separate files:

- `<Feature>DataHiltModule.kt` — `@Module @InstallIn(SingletonComponent::class) internal object`
- `<Feature>DomainHiltModule.kt` — `@Module @InstallIn(SingletonComponent::class) internal object`
- `<Feature>PresentationHiltModule.kt` — only if non-VM bindings exist (error mappers, factories)
- No aggregator needed — Hilt auto-discovers `@Module` classes

### Composable Injection

- Use `hiltViewModel()` for ViewModels
- For non-VM dependencies in Composables, use `@EntryPoint @InstallIn(SingletonComponent::class)` interfaces with `EntryPointAccessors.fromApplication()`

## Dispatcher Usage

Dispatchers are injected via `@IoDispatcher`/`@DefaultDispatcher`/`@MainDispatcher` qualifiers, never hardcoded:

| Layer      | Dispatcher                          |
|------------|-------------------------------------|
| ViewModel  | `dispatcherDefault`                 |
| Mapper     | `dispatcherDefault`                 |
| Repository | delegates (or `dispatcherDefault`)  |
| DataSource | `dispatcherIo`                      |

## Database

Centralized Room database in the `app` module (`AppDatabase`):

- `DestinationDao` — cycling POI data
- `LocaleDao` — language/locale settings
- `PairedDeviceDao` — paired BLE devices
- `ProfileDao` — rider profile data
- `SessionDao` — session and track points

`@Dao` interfaces use suspend functions for one-shot operations and `Flow` for observable queries. Default conflict strategy: `OnConflictStrategy.REPLACE`.

## Data Persistence

| Storage   | Purpose                                                  |
|-----------|----------------------------------------------------------|
| Room      | Destinations, sessions, track points, locale, profile, paired devices |
| DataStore | User preferences (theme, language, profile, stats display configuration) |
