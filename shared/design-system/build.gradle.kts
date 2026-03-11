plugins {
    id("cycling.library")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.designsystem"

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.ui)
    testFixturesImplementation(libs.mockk)
}
