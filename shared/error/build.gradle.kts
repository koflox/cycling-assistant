plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.error"
}

dependencies {
    implementation(project(":shared:concurrent"))
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
