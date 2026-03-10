plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.koflox.map"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.play.services.maps)
    implementation(project(":shared:graphics"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
