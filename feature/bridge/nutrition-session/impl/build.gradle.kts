plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.nutritionsession.bridge.impl"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    implementation(project(":feature:nutrition"))
    implementation(project(":feature:bridge:nutrition-session:api"))
    implementation(project(":feature:session"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
