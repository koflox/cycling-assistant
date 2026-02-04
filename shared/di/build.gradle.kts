plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.di"
}

dependencies {
    implementation(libs.koin.core)
}
