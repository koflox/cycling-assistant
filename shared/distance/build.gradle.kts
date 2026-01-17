plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.koflox.distance"
}

dependencies {
    implementation(libs.koin.core)
    testImplementation(libs.junit)
}
