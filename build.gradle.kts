// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.module.graph)
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
    exclude("**/build/**", "**/resources/**")
    reports {
        xml.required.set(true)
        html.required.set(false)
        txt.required.set(false)
    }
}

// ===========================================
// Subprojects Common Configuration
// ===========================================

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    val compileSdkVersion = rootProject.libs.versions.compileSdk.get().toInt()
    val minSdkVersion = rootProject.libs.versions.minSdk.get().toInt()
    val javaVersion = JavaVersion.toVersion(rootProject.libs.versions.javaVersion.get())

    plugins.withId("com.android.library") {
        configure<com.android.build.api.dsl.LibraryExtension> {
            compileSdk = compileSdkVersion

            defaultConfig {
                minSdk = minSdkVersion
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }

            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
    }

    plugins.withId("com.android.application") {
        configure<com.android.build.api.dsl.ApplicationExtension> {
            compileSdk = compileSdkVersion

            defaultConfig {
                minSdk = minSdkVersion
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }

            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
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
