plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.altitude"
}

dependencies {
    testImplementation(libs.junit)
}
