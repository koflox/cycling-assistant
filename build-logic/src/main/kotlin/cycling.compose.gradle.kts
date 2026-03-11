import com.android.build.api.dsl.LibraryExtension
import com.koflox.build.library
import com.koflox.build.libs

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<LibraryExtension> {
    buildFeatures {
        compose = true
    }
}

dependencies {
    "implementation"(platform(libs.library("androidx-compose-bom")))
    "implementation"(libs.library("androidx-ui"))
    "implementation"(libs.library("androidx-ui-graphics"))
    "implementation"(libs.library("androidx-ui-tooling-preview"))
    "implementation"(libs.library("androidx-material3"))
    "implementation"(libs.library("androidx-material-icons-extended"))

    "debugImplementation"(libs.library("androidx-ui-tooling"))
}
