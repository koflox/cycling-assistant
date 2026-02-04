plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.distance"
}

dependencies {
    implementation(libs.koin.core)
    testImplementation(libs.junit)
}
