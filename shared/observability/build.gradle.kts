plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.observability"
}

dependencies {
    implementation(libs.androidx.startup.runtime)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":shared:concurrent"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
