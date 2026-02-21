# Concurrency

All concurrency primitives live in `shared/concurrent`.

## Dispatcher Injection

Dispatchers are never hardcoded. They are registered as Koin singletons via `DispatchersQualifier`
and injected into constructors:

```kotlin
sealed class DispatchersQualifier : ClassNameQualifier(), Qualifier {
    object Io : DispatchersQualifier()
    object Main : DispatchersQualifier()
    object Default : DispatchersQualifier()
    object Unconfined : DispatchersQualifier()
}
```

Each layer uses a specific dispatcher:

| Layer      | Dispatcher          | Why |
|------------|---------------------|-----|
| ViewModel  | `dispatcherDefault` | CPU-bound state transformations |
| Mapper     | `dispatcherDefault` | CPU-bound data mapping |
| Repository | delegates (or `dispatcherDefault`) | Coordinates, rarely does own work |
| DataSource | `dispatcherIo`      | Disk and network I/O |

## suspendRunCatching

A coroutine-safe replacement for `runCatching`. Standard `runCatching` catches
`CancellationException`, which breaks structured concurrency — a cancelled coroutine silently
returns `Result.failure` instead of propagating cancellation.

```kotlin
inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e  // preserve structured concurrency
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

Used consistently in repositories and use cases. Repository suspend functions return `Result<T>`.
Observable functions return `Flow<T>` (no `Result` wrapping for flows).

## ConcurrentFactory

A generic abstract factory with suspendable `get()` and double-checked locking:

```kotlin
abstract class ConcurrentFactory<T : Any> {
    @Volatile
    private var instance: T? = null
    private val mutex = Mutex()

    suspend fun get(): T {
        instance?.let { return it }
        return mutex.withLock {
            instance ?: create().also { instance = it }
        }
    }

    protected abstract suspend fun create(): T
}
```

**Design decisions:**

- `Mutex` (coroutine-friendly) instead of `synchronized` — `create()` may do I/O, blocking a
  thread would be wasteful
- `@Volatile` for fast-path visibility before the lock — subsequent calls skip the mutex entirely
- If `create()` throws, `instance` stays `null` — next `get()` retries

### Database Initialization

`RoomDatabaseFactory` extends `ConcurrentFactory<AppDatabase>`. Room initialization involves disk
I/O, schema setup, and (in release builds) loading the SQLCipher native library and performing
cryptographic operations. The `create()` method runs within `withContext(dispatcherIo)`,
guaranteeing background execution regardless of the caller's context.

Feature modules access DAOs through `ConcurrentFactory<*Dao>` instances injected via Koin
qualifiers. The first `daoFactory.get()` call triggers lazy database creation; subsequent calls
return the cached instance via a volatile read (no lock contention).

```kotlin
internal class LocaleRoomDataSource(
    private val daoFactory: ConcurrentFactory<LocaleDao>,
    private val dispatcherIo: CoroutineDispatcher,
) : LocaleLocalDataSource {

    override suspend fun saveLanguage(languageTag: String) = withContext(dispatcherIo) {
        daoFactory.get().upsertLocaleSettings(LocaleSettingsEntity(languageTag = languageTag))
    }

    override fun observeLanguage(): Flow<String?> = flow {
        emitAll(daoFactory.get().observeLocaleSettings().map { it?.languageTag })
    }
}
```

For `Flow`-returning methods, the `flow { emitAll(...) }` wrapper provides a suspend context
for the `daoFactory.get()` call.

## lazyUnsafe

A shorthand for `lazy(mode = LazyThreadSafetyMode.NONE)`. Use when thread safety is guaranteed
by the call site (e.g., single-threaded access or already synchronized).

```kotlin
fun <T> lazyUnsafe(initializer: () -> T) = lazy(mode = LazyThreadSafetyMode.NONE) { initializer.invoke() }
```
