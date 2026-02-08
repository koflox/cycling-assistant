plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.profilesession.bridge.impl"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Koin
    implementation(libs.koin.core)

    // Bridge API
    implementation(project(":feature:bridge:profile-session:api"))
    implementation(project(":feature:profile"))
}
