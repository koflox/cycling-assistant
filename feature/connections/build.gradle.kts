plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.connections"
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Feature modules
    implementation(project(":feature:sensor:power"))

    // Shared modules
    implementation(project(":shared:ble"))
    implementation(project(":shared:error"))
    implementation(project(":shared:id"))
    implementation(project(":shared:sensor-protocol"))
}
