plugins {
    id("cycling.bridge.impl")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationnutrition.bridge.impl"
}

dependencies {
    implementation(project(":feature:bridge:destination-nutrition:api"))
    implementation(project(":feature:nutrition"))
}
