import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.android.library) apply false
}

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

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
    afterEvaluate {
        extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
            compileSdkVersion(36)

            defaultConfig {
                minSdk = 24
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }

        extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension>()?.apply {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    subprojects.forEach { subproject ->
        kover(subproject)
    }
}

kover {
    reports {
        filters {
            excludes {
                // Exclude generated code and common non-testable patterns
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*.BuildConfig",
                    "*.databinding.*",
                    "*ComposableSingletons\$*",
                    "*_Impl",
                    "*_Impl\$*",
                )
                // Exclude DI modules and navigation
                packages(
                    "*.di",
                    "*.navigation",
                )
                // Exclude annotated classes
                annotatedBy(
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.ui.tooling.preview.Preview",
                )
            }
        }
    }
}
