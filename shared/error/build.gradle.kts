plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
