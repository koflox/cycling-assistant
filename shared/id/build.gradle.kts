plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.id"
}

dependencies {
    testImplementation(libs.junit)
}
