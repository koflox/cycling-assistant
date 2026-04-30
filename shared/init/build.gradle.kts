plugins {
    id("cycling.library")
    id("cycling.hilt")
}

android {
    namespace = "com.koflox.init"
}

dependencies {
    implementation(project(":shared:altitude"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:id"))

    implementation(libs.kotlinx.coroutines.android)
}
