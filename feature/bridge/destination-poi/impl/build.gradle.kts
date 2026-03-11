plugins {
    id("cycling.bridge.impl")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationpoi.bridge.impl"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(project(":feature:bridge:destination-poi:api"))
    implementation(project(":feature:poi"))
}
