plugins {
    id("cycling.library")
}

android {
    namespace = "com.koflox.graphics"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
