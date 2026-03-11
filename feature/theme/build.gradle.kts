plugins {
    id("cycling.library")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

android {
    namespace = "com.koflox.theme"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Shared modules
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))
}
