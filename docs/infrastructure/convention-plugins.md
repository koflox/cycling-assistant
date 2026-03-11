# Convention Plugins

Shared build configuration lives in `build-logic/` — a separate Gradle project (included build)
that produces reusable plugins. This replaces the `subprojects {}` approach.

## Why Convention Plugins?

With 44+ modules, each `build.gradle.kts` repeated the same plugins, android config, and
dependencies. Convention plugins extract these patterns into composable units:

| Problem | Before | After |
|---|---|---|
| Adding Hilt to a module | 2 plugins + 3 dependency lines | `id("cycling.hilt")` |
| Full feature module setup | ~50 lines of boilerplate | `id("cycling.feature")` + namespace |
| Changing a common dependency | Edit ~15 files | Edit 1 convention plugin |

## Plugin Hierarchy

Plugins are composable — higher-level ones include lower-level ones:

```
cycling.library              ← base for all library modules
├── cycling.compose          ← + Compose (plugin + BOM + material3)
├── cycling.hilt             ← + Hilt (ksp + hilt plugins + deps)
├── cycling.testing.unit     ← + unit test deps (junit, mockk, turbine, etc.)
│
cycling.feature              ← library + compose + hilt + testing.unit
│                               + lifecycle + navigation + coroutines
│                               + shared:{concurrent, design-system, di}
│
cycling.bridge.api           ← library (minimal)
cycling.bridge.impl          ← library + hilt + testing.unit + coroutines
```

### What Each Plugin Provides

**`cycling.library`** — applied to every library module:

- `com.android.library` plugin
- `compileSdk`, `minSdk`, Java version (from version catalog)
- `testInstrumentationRunner`, `consumerProguardFiles`
- `org.jetbrains.kotlinx.kover` (code coverage)

**`cycling.compose`** — additive, for modules with UI:

- `org.jetbrains.kotlin.plugin.compose`
- `buildFeatures { compose = true }`
- Compose BOM, `ui`, `ui-graphics`, `ui-tooling-preview`, `material3`, `material-icons-extended`
- `debugImplementation` for `ui-tooling`

**`cycling.hilt`** — additive, for modules with DI:

- `com.google.devtools.ksp` + `com.google.dagger.hilt.android` plugins
- `hilt-android` (implementation) + `hilt-compiler` (ksp)

**`cycling.testing.unit`** — additive, standard test stack:

- `junit`, `kotlinx-coroutines-test`, `mockk`, `turbine`, `shared:testing`

**`cycling.feature`** — composite, the most common:

- Includes: `cycling.library` + `cycling.compose` + `cycling.hilt` + `cycling.testing.unit`
- Adds: `core-ktx`, lifecycle (3 libs), `navigation-compose`, coroutines (core + android),
  `hilt-navigation-compose`
- Adds: `shared:concurrent`, `shared:design-system`, `shared:di`
- Adds: androidTest + debug dependencies

**`cycling.bridge.api`** — minimal wrapper:

- Includes: `cycling.library`

**`cycling.bridge.impl`** — bridge with DI and tests:

- Includes: `cycling.library` + `cycling.hilt` + `cycling.testing.unit`
- Adds: `kotlinx-coroutines-core`

### Module → Plugin Mapping

| Plugin | Modules |
|---|---|
| `cycling.feature` | connections, destinations, nutrition, poi, sensor:power, session, settings |
| `cycling.library` + `cycling.compose` | dashboard, design-system |
| `cycling.library` + `cycling.hilt` | altitude, ble, concurrent, distance, error, id, observability, map |
| `cycling.library` + `cycling.hilt` + `cycling.testing.unit` | locale, profile, theme, location |
| `cycling.library` only | di, graphics, sensor-protocol, testing |
| `cycling.bridge.api` | 9 bridge API modules |
| `cycling.bridge.impl` | 9 bridge impl modules |
| Not managed (unique) | app, baselineprofile |

## How It Works

### build-logic as an Included Build

`build-logic/` is a **separate Gradle project** connected via `includeBuild`:

```kotlin
// settings.gradle.kts (root)
pluginManagement {
    includeBuild("build-logic")  // ← register as plugin source
}
```

Gradle compiles `build-logic` first, registers its plugins, then builds the main project.
Each project has its own `settings.gradle.kts`:

| | Root `settings.gradle.kts` | `build-logic/settings.gradle.kts` |
|---|---|---|
| Role | Main project config | Build plugin project config |
| Includes | 44 app modules | Nothing (single-module) |
| Version catalog | Auto from `gradle/libs.versions.toml` | Explicit `from(files("../gradle/libs.versions.toml"))` |
| Repositories | google + mavenCentral | google + mavenCentral + **gradlePluginPortal** |

`build-logic` needs `gradlePluginPortal()` because it downloads Gradle plugins as regular
library JARs.

### build-logic/build.gradle.kts

```kotlin
plugins {
    `kotlin-dsl`  // enables precompiled script plugins
}
dependencies {
    compileOnly(libs.android.gradlePlugin)   // AGP classes (LibraryExtension, etc.)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.composePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
}
```

**`kotlin-dsl`** — built-in Gradle plugin that:

1. Enables Kotlin in `src/main/kotlin/`
2. Makes `*.gradle.kts` files in `src/main/kotlin/` into plugins (filename = plugin ID)
3. Adds Gradle API to classpath (`Project`, `DependencyHandler`, etc.)

**`compileOnly` dependencies** — Gradle plugin JARs needed at compile time so convention plugins
can reference their classes (`LibraryExtension`, KSP configuration, etc.). `compileOnly` instead
of `implementation` because the actual plugins are resolved at runtime through `pluginManagement`.

These plugin artifacts must be declared in the version catalog:

```toml
# gradle/libs.versions.toml
[libraries]
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
# ...
```

### Version Catalog Access

In regular modules, Gradle generates type-safe accessors:

```kotlin
// module build.gradle.kts — type-safe
libs.versions.compileSdk.get()
libs.hilt.android
alias(libs.plugins.android.library)
implementation(libs.hilt.android)
```

In convention plugins, these accessors don't exist (the plugin compiles before any module).
String-based API is used instead:

```kotlin
// convention plugin — string-based
libs.version("compileSdk")                     // same as libs.versions.compileSdk.get()
libs.library("hilt-android")                   // same as libs.hilt.android
plugins.apply("com.android.library")           // same as alias(libs.plugins.android.library)
"implementation"(libs.library("hilt-android")) // same as implementation(libs.hilt.android)
```

A helper in `Extensions.kt` provides shortcuts:

```kotlin
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

internal fun VersionCatalog.library(alias: String) =
    findLibrary(alias).get()
```

### How `"implementation"(...)` Works

Gradle's Kotlin DSL defines an `operator fun` on `String` inside the `dependencies {}` scope:

```kotlin
// Defined in Gradle Kotlin DSL (not generated — always available)
operator fun String.invoke(dependency: Any): Dependency? =
    dependencies.add(this, dependency)
```

So `"implementation"(dep)` compiles to `"implementation".invoke(dep)`, which calls
`dependencies.add("implementation", dep)`. This is the same underlying API that the generated
`implementation(dep)` calls — just without compile-time validation of the configuration name.

Configuration names (`implementation`, `ksp`, `testImplementation`, etc.) are registered by
plugins at runtime. A typo like `"implmentation"(dep)` compiles but fails at runtime with
`UnknownConfigurationException`.

## Adding a New Convention Plugin

1. Create `build-logic/src/main/kotlin/<plugin-id>.gradle.kts`
2. The filename becomes the plugin ID (e.g., `cycling.foo.gradle.kts` → `id("cycling.foo")`)
3. If it needs new Gradle plugin classes, add `compileOnly` dep in `build-logic/build.gradle.kts`
4. Apply in modules via `plugins { id("cycling.foo") }`
