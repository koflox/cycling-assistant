plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.gpx"
}

dependencies {
    testImplementation(libs.junit)
}
