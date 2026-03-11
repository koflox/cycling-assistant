plugins {
    id("cycling.library")
    id("cycling.compose")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

android {
    namespace = "com.koflox.location"
}

dependencies {
    implementation(libs.play.services.location)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))
}
