plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.distance"
}

dependencies {
    testImplementation(libs.junit)
}
