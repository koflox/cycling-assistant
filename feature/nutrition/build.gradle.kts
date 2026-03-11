plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.nutrition"
}

dependencies {
    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Bridge modules
    implementation(project(":feature:bridge:nutrition-session:api"))
}
