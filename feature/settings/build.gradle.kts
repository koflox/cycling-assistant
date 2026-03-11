plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.settings"
}

dependencies {
    implementation(libs.androidx.appcompat)

    // Feature modules
    implementation(project(":feature:theme"))
    implementation(project(":feature:locale"))
    implementation(project(":feature:profile"))

    // Bridge modules
    implementation(project(":feature:bridge:nutrition-settings:api"))
    implementation(project(":feature:bridge:poi-settings:api"))
    implementation(project(":feature:bridge:session-settings:api"))
}
