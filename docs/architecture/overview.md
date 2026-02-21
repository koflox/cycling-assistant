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

## Dependency Injection (Koin)

All dependencies are managed via [Koin](https://insert-koin.io/). Registration conventions:

| Type       | Scope                                                           |
|------------|-----------------------------------------------------------------|
| UseCase    | `factory` (`single` if stateful — e.g. internal buffer/smoother) |
| DataSource | `single`                                                        |
| Mapper     | `single`                                                        |
| Repository | `single`                                                        |
| ViewModel  | `viewModel { }`                                                 |

### Feature-Local Qualifiers

When a feature needs to disambiguate DI bindings internally, define a `sealed class` extending `ClassNameQualifier()` in the feature's `di/` package (e.g., `SessionQualifier.SessionMutex` for a shared `Mutex`).

### DI File Organization

Each feature module organizes DI into separate files:

- `DataModule.kt` — `private val dataModule`, `private val dataSourceModule`, `private val repoModule`, exported as `internal val dataModules: List<Module>`
- `DomainModule.kt` — `internal val domainModule`
- `PresentationModule.kt` — `internal val presentationModule`
- `<Feature>Module.kt` — public aggregator: `val featureModule = module { includes(...) }`

## Dispatcher Usage

Dispatchers are injected via `DispatchersQualifier`, never hardcoded:

| Layer      | Dispatcher                          |
|------------|-------------------------------------|
| ViewModel  | `dispatcherDefault`                 |
| Mapper     | `dispatcherDefault`                 |
| Repository | delegates (or `dispatcherDefault`)  |
| DataSource | `dispatcherIo`                      |

## Database

Centralized Room database in the `app` module (`AppDatabase`):

- `DestinationDao` — cycling POI data
- `SessionDao` — session and track points

`@Dao` interfaces use suspend functions for one-shot operations and `Flow` for observable queries. Default conflict strategy: `OnConflictStrategy.REPLACE`.

## Data Persistence

| Storage   | Purpose                                  |
|-----------|------------------------------------------|
| Room      | Destinations, sessions, and track points |
| DataStore | User preferences (theme, language, profile) |
