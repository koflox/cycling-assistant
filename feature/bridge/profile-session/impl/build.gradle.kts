plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.koflox.profilesession.bridge.impl"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Bridge API
    implementation(project(":feature:bridge:profile-session:api"))
    implementation(project(":feature:profile"))
}
