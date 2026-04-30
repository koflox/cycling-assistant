plugins {
    id("cycling.bridge.api")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationsession.bridge"
}

dependencies {
    implementation(project(":shared:location:domain"))
    implementation(libs.kotlinx.coroutines.core)
}
