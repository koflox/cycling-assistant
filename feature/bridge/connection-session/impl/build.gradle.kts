plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.connectionsession.bridge.impl"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    implementation(project(":feature:bridge:connection-session:api"))
    implementation(project(":feature:connections"))
    implementation(project(":feature:sensor:power"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
