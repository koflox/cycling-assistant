# Stability Cheatsheet

The commands to run to confirm the project is stable, in the order to run them. Each links to its
detailed documentation.

| Command | What it verifies | Docs |
|---|---|---|
| `./gradlew detektRun` | Static analysis & formatting — zero-issue policy, auto-correct enabled. | [Code Quality → Detekt](code-quality.md#detekt) |
| `./gradlew testDebugUnitTest` | JVM unit tests (debug variant) — ViewModels, use cases, mappers, repositories. No device needed. | [Testing → Unit Tests](testing.md#unit-tests) |
| `./gradlew connectedDebugAndroidTest` | Instrumented Compose UI tests. **Requires a connected device/emulator.** | [Testing → UI Tests](testing.md#ui-tests) |
| `./gradlew verifyRoborazziDebug` | Screenshot regression vs. golden images (JVM, no emulator). | [Testing → Screenshot Tests](testing.md#screenshot-tests) |

## Notes

- **Order matters for speed of feedback:** `detektRun` and `testDebugUnitTest` are fast and need no
  device — run them first. `connectedDebugAndroidTest` needs a device/emulator. Screenshot
  verification is JVM-based.
- **Detekt is zero-tolerance** with auto-correct enabled — run it (max twice) until clean before
  pushing. See [Code Quality](code-quality.md).
- **Screenshot tests** — run `verifyRoborazziDebug` on each module that applies the
  `cycling.testing.screenshot` plugin. When a UI change is intentional, re-record goldens with
  `./gradlew :module:recordRoborazziDebug` and commit the updated images. Replace `:module` with the target, e.g. `:feature:session:tracking`. See
  [Screenshot Tests](testing.md#screenshot-tests).
- **CI runs the same checks** — see [CI/CD](ci-cd.md) for the pipeline that gates merges.
