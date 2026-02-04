plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.id"
}

dependencies {
    implementation(libs.koin.core)
    testImplementation(libs.junit)
}
