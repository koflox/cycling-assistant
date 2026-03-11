plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.sensor.power"
}

dependencies {
    // Shared modules
    implementation(project(":shared:ble"))
    implementation(project(":shared:error"))
    implementation(project(":shared:sensor-protocol"))
}
