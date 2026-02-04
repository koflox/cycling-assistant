plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.altitude"
}

dependencies {
    implementation(libs.koin.core)
    testImplementation(libs.junit)
}
