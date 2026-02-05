plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.koflox.destinationsession.bridge"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared:location"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.kotlinx.coroutines.core)
}
