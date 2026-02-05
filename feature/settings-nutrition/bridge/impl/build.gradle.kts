plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.koflox.settingsnutrition.bridge.impl"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)

    // Bridge API
    implementation(project(":feature:settings-nutrition:bridge:api"))
    implementation(project(":feature:nutrition"))
}
