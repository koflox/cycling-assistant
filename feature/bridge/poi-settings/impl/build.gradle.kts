plugins {
    id("cycling.bridge.impl")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.poisettings.bridge.impl"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(project(":feature:bridge:poi-settings:api"))
    implementation(project(":feature:poi"))
}
