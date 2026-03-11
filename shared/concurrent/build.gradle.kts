plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.concurrent"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(project(":shared:di"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
