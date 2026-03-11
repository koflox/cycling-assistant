plugins {
    id("cycling.bridge.api")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationsession.bridge"
}

dependencies {
    implementation(project(":shared:location"))
    implementation(libs.kotlinx.coroutines.core)
}
