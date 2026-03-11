// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf.plugin) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.module.graph)
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
}

// ===========================================
// Detekt
// ===========================================

dependencies {
    detektPlugins(libs.detekt)
}

tasks.register("detektRun", io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Runs detekt analysis over whole code base"
    parallel = true
    autoCorrect = true
    config.setFrom(files("$rootDir/.lint/detekt/detekt-config.yml"))
    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/resources/**", "**/build-logic/**")
    reports {
        xml.required.set(true)
        html.required.set(false)
        txt.required.set(false)
    }
}

// ===========================================
// Kover (Code Coverage)
// ===========================================

dependencies {
    subprojects.forEach { subproject ->
        kover(subproject)
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*.BuildConfig",
                    "*.databinding.*",
                    "*ComposableSingletons\$*",
                    "*_Impl",
                    "*_Impl\$*",
                    "*Activity",
                    "*Activity\$*",
                    "*DataStore",
                    "*DataStore\$*",
                    "*RoomDataSource",
                    "*RoomDataSource\$*",
                    "*DatabasePassphraseManager",
                    "*LocalizedContextProviderImpl",
                    "Hilt_*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                )
                packages(
                    "*.di",
                    "*.navigation",
                    "*.dialog",
                )
                annotatedBy(
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.ui.tooling.preview.Preview",
                )
            }
        }
    }
}

// ===========================================
// Module Graph
// ===========================================

moduleGraphConfig {
    readmePath.set("./docs/MODULE_GRAPH.md")
    heading.set("# Module Dependency Graph")
    theme.set(dev.iurysouza.modulegraph.Theme.NEUTRAL)
    showFullPath.set(false)
    rootModulesRegex.set(".*:app")
    excludedModulesRegex.set(".*:testing")
}
