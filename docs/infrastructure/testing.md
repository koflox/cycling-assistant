# Testing

## Test Structure

Tests live in `src/test/java` within each module. Naming convention: `<ClassName>Test.kt`.

**Dependencies:** JUnit 4, MockK, Turbine, kotlinx-coroutines-test, `shared:testing`

## ViewModel Test Pattern

```kotlin
class FeatureViewModelTest {

    companion object {
        private const val TEST_ID = "test-123"
        private const val ERROR_MESSAGE = "Something went wrong"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase: FeatureUseCase = mockk()
    private val errorMapper: ErrorMessageMapper = mockk()
    private lateinit var viewModel: FeatureViewModel

    @Before
    fun setup() { setupDefaultMocks() }

    private fun setupDefaultMocks() {
        coEvery { errorMapper.map(any()) } returns ERROR_MESSAGE
    }

    private fun createViewModel() = FeatureViewModel(
        useCase = useCase,
        errorMapper = errorMapper,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { useCase.getData() } returns Result.success(createData())
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is FeatureUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## Conventions

- `companion object` for test constants at the top of the class
- `@get:Rule` for `MainDispatcherRule` (from `shared:testing`)
- Mocks declared as class properties, `@Before` for common mock setup
- `createViewModel()` factory method for consistent initialization
- No region comments — test names should be self-documenting
- Backtick names: `` `action/condition expected result` ``

## Common MockK Patterns

| Pattern                            | Usage                                      |
|------------------------------------|--------------------------------------------|
| `coEvery { ... }`                  | Mock suspend functions                     |
| `every { ... }`                    | Mock regular functions                     |
| `coVerify { ... }`                 | Verify suspend function calls              |
| `justRun { ... }`                  | Mock Unit-returning functions              |
| `slot<T>()`                        | Capture arguments for verification         |
| `awaitItem()`                      | Wait for next Flow emission (Turbine)      |
| `cancelAndIgnoreRemainingEvents()` | End test without consuming remaining items |

## Test Factory Functions

Factory functions create test data with sensible defaults. All parameters have empty/zero defaults (empty strings, `0`, `0.0`, `emptyList()`, etc.). Tests pass explicit values using constants from `companion object` — no inline magic values.

| Scope        | Location                                | Consumed via                            |
|--------------|-----------------------------------------|-----------------------------------------|
| Cross-module | `src/testFixtures/kotlin/.../testutil/` | `testImplementation(testFixtures(...))` |
| Module-local | `src/test/java/.../testutil/`           | Direct import                           |

To enable `testFixtures` for a module, add to its `build.gradle.kts`:

```kotlin
android {
    testFixtures {
        enable = true
    }
}
```
