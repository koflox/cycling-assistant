import com.koflox.convention.library
import com.koflox.convention.libs

plugins {
    id("cycling.library")
    id("cycling.compose")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

dependencies {
    // Hilt Navigation Compose (ViewModel injection in Composables)
    "implementation"(libs.library("hilt-navigation-compose"))

    // Core Android
    "implementation"(libs.library("androidx-core-ktx"))

    // ViewModels & Lifecycle
    "implementation"(libs.library("androidx-lifecycle-viewmodel-ktx"))
    "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
    "implementation"(libs.library("androidx-lifecycle-runtime-compose"))

    // Navigation
    "implementation"(libs.library("androidx-navigation-compose"))

    // Coroutines
    "implementation"(libs.library("kotlinx-coroutines-core"))
    "implementation"(libs.library("kotlinx-coroutines-android"))

    // Common shared modules
    "implementation"(project(":shared:concurrent"))
    "implementation"(project(":shared:design-system"))
    "implementation"(project(":shared:di"))

    // UI Testing
    "androidTestImplementation"(libs.library("androidx-junit"))
    "androidTestImplementation"(libs.library("androidx-ui-test-junit4"))
    "debugImplementation"(libs.library("androidx-ui-test-manifest"))
}
