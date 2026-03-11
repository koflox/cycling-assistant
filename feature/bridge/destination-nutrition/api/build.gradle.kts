plugins {
    id("cycling.bridge.api")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationnutrition.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
