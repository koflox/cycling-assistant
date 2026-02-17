# Bridge Pattern

Features in CyclingAssistant are fully isolated — they never depend on each other directly. When two features need to communicate, they do so through a **bridge module**.

## Structure

Each bridge lives under `feature/bridge/` and is named as an alphabetically-ordered pair of the two features it connects:

- `destination-nutrition` (destinations ↔ nutrition)
- `destination-session` (destinations ↔ session)
- `nutrition-session` (nutrition ↔ session)
- `nutrition-settings` (nutrition ↔ settings)
- `profile-session` (profile ↔ session)

Every bridge contains two submodules:

| Submodule | Contains                                    | Depended on by |
|-----------|---------------------------------------------|----------------|
| `api/`    | Interfaces (use cases, UI navigators)       | Consumer feature |
| `impl/`   | Implementations wiring to provider internals | `app` module    |

## Data Bridges

When feature A needs data from feature B, the bridge API defines a use case interface:

```
feature:destinations ──depends on──> feature:bridge:destination-session:api
                                            │
                                            │ (interface)
                                            ▼
                                     CyclingSessionUseCase
                                            │
                                            │ (implemented by)
                                            ▼
feature:session <──depends on── feature:bridge:destination-session:impl
```

The consumer (destinations) depends on the `api` submodule and calls the interface. The `impl` submodule depends on the provider (session) and wires the implementation. The `app` module includes both `api` and `impl` in its dependency graph.

## UI Bridges

When feature A needs to display UI from feature B, the bridge API defines a composable navigator interface using callbacks (never `NavController`):

```kotlin
// Bridge API — uses callbacks, not NavController
interface CyclingSessionUiNavigator {
    @Composable
    fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
        onNavigateToCompletion: (sessionId: String) -> Unit,
    )
}
```

### Naming Conventions

| Context               | Naming                                               |
|-----------------------|------------------------------------------------------|
| Bridge interface      | Descriptive name: `SessionScreen`, `DestinationOptions` |
| Feature implementation | Route suffix: `SessionScreenRoute`, `DestinationOptionsRoute` |

## DI Registration

Bridge `impl` modules register their implementations in Koin. The DI module name follows the pattern `<aB>BridgeImplModule`:

- `destinationSessionBridgeImplModule`
- `nutritionSessionBridgeImplModule`
- `profileSessionBridgeImplModule`

## Adding a New Bridge

1. Create the bridge under `feature/bridge/<A-B>/` (A and B in alphabetical order) with `api/` and `impl/` submodules
2. Define interfaces in `api/`
3. Implement in `impl/`, depending on the provider feature
4. Consumer feature depends on the `api` submodule
5. Register DI in the `impl` module and include in `app/Modules.kt`
