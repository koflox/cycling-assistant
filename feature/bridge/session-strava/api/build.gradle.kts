plugins {
    id("cycling.bridge.api")
}

android {
    namespace = "com.koflox.sessionstrava.bridge"
}

dependencies {
    implementation(project(":shared:gpx"))
}
