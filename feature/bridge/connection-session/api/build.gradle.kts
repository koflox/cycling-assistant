plugins {
    id("cycling.bridge.api")
}

android {
    namespace = "com.koflox.connectionsession.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
