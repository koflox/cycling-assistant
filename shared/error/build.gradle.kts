plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.error"
}

dependencies {
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:design-system"))
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(project(":shared:testing"))
}
