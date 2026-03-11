plugins {
    id("cycling.bridge.api")
}

android {
    namespace = "com.koflox.nutritionsession.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
