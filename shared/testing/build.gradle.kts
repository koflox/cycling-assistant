plugins {
    id("cycling.library")
}

android {
    namespace = "com.koflox.testing"
}

dependencies {
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
}
