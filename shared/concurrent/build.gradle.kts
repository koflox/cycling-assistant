plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.concurrent"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(project(":shared:di"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
