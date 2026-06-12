# Code Quality

## Detekt

The project enforces zero Detekt issues with auto-correct enabled.

```bash
./gradlew detektRun
```

Key rules:

- **ReturnCount** — max 2 explicit returns per function. Use `if/else` or `when` to avoid multiple early returns.
- **Max line length** — 150 characters
- **Trailing commas** — required on declarations

## Kover

Code coverage is generated via Kover and reported on PRs by the CI pipeline.

```bash
./gradlew koverXmlReport
```

Coverage thresholds for badge coloring:

| Coverage   | Color  |
|------------|--------|
| >= 60%     | Green  |
| >= 30%     | Yellow |
| < 30%      | Red    |

## Code Conventions

### Naming

- **Boolean properties** — prefix with `is`, `has`, `are`
- **Companion object** — place at the top of the class body
- **String resources** — follow `feature_component_description` pattern (e.g., `session_stat_*`, `notification_*`, `dialog_*`)

### Method Reuse Within a Class

Never call a `public`/`override` method from another method in the same class to reuse its
logic. Extract the shared body into a `private fun` and call that from both places.

**Why:** when the public method's behavior later changes, the impact must be explicit. A
public → public internal call hides the coupling — editing one public method silently changes
the behavior of every sibling that called it.

```kotlin
// ✗ Bad — reconcileStatus silently depends on refreshStatus's public contract
override suspend fun refreshStatus(sessionId: String) {
    val uploadId = syncRepository.getUploadId(sessionId) ?: return
    uploadRepository.getUploadStatus(uploadId).onSuccess { applyUploadStatus(sessionId, it) }
}
override suspend fun reconcileStatus(sessionId: String) {
    if (/* ... */) refreshStatus(sessionId)
}

// ✓ Good — both delegate to a shared private fun
override suspend fun refreshStatus(sessionId: String) {
    internalRefreshStatus(sessionId)
}
override suspend fun reconcileStatus(sessionId: String) {
    if (/* ... */) internalRefreshStatus(sessionId)
}
private suspend fun internalRefreshStatus(sessionId: String) {
    val uploadId = syncRepository.getUploadId(sessionId) ?: return
    uploadRepository.getUploadStatus(uploadId).onSuccess { applyUploadStatus(sessionId, it) }
}
```

### Visibility Modifiers

| Element                                          | Visibility   |
|--------------------------------------------------|--------------|
| Domain use case interfaces                       | `public` if cross-module, `internal` if module-local |
| Domain repository interfaces                     | `public`     |
| Data layer interfaces (DataSource, Mapper)       | `internal`   |
| All `*Impl` classes                              | `internal`   |
| All ViewModels                                   | `internal` (`@HiltViewModel internal class ... @Inject constructor`) |
| Hilt `@Module` objects                           | `internal`   |

### Composable Conventions

| Layer          | Naming Pattern  | Visibility | ViewModel        |
|----------------|-----------------|------------|------------------|
| Entry point    | `<Name>Route`   | `internal` | Obtained via DI  |
| Screen content | `<Name>Content` | `private`  | Passed as params |

- Only expose publicly required params (e.g., `onBackClick`, `onNavigateTo...`)
- Content-level composables should have previews for all their states
- No blank lines between composable body elements

### Build Conventions (Convention Plugins)

- Shared build configuration lives in `build-logic/` as precompiled script plugins
- Feature modules apply `id("cycling.feature")` — provides Compose, Hilt, testing, lifecycle,
  navigation, coroutines, and common shared modules
- Bridge modules apply `id("cycling.bridge.api")` or `id("cycling.bridge.impl")`
- Shared modules combine base plugins as needed: `cycling.library`, `cycling.compose`, `cycling.hilt`
- Modules in `settings.gradle.kts` are alphabetically sorted

