plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.koflox.sessionsettings.bridge.impl"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Koin
    implementation(libs.koin.core)

    // Bridge API
    implementation(project(":feature:session-settings:bridge:api"))
    implementation(project(":feature:profile"))
}
