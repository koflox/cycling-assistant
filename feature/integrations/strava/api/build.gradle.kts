plugins {
    id("cycling.bridge.api")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.strava.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
