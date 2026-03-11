plugins {
    id("cycling.bridge.impl")
}

android {
    namespace = "com.koflox.nutritionsession.bridge.impl"
}

dependencies {
    implementation(project(":feature:nutrition"))
    implementation(project(":feature:bridge:nutrition-session:api"))
    implementation(project(":feature:session"))
}
