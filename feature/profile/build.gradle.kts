plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.profile"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Shared modules
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
