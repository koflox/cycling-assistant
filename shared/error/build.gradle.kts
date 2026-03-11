plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.error"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:design-system"))
    implementation(project(":shared:di"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(project(":shared:testing"))
}
