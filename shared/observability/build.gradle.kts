plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.observability"

    defaultConfig {
        // Default for library scope (incl. androidTest). App overrides per buildType.
        manifestPlaceholders["observabilityCollectionEnabled"] = "false"
    }
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
