plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.koflox.altitude"
}

dependencies {
    implementation(libs.koin.core)
    testImplementation(libs.junit)
}
