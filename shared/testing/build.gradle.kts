plugins {
    id("cycling.kotlin.library")
}

dependencies {
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
}
