plugins {
    id("cycling.library")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

android {
    namespace = "com.koflox.locale"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Shared modules
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))
}
