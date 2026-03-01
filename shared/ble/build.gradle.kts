plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.ble"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.android)
    implementation(project(":shared:concurrent"))

    testImplementation(libs.junit)
}
