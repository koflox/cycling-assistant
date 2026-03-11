plugins {
    id("cycling.library")
    id("cycling.compose")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.map"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.maps)
    implementation(project(":shared:graphics"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
