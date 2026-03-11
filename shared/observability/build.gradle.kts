plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.observability"
}

dependencies {
    implementation(libs.androidx.startup.runtime)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    api(libs.timber)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":shared:concurrent"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
