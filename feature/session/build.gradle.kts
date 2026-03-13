plugins {
    id("cycling.feature")
    id("cycling.testing.screenshot")
}

android {
    namespace = "com.koflox.session"

    testFixtures {
        enable = true
        androidResources = true
    }
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Permissions
    implementation(libs.accompanist.permissions)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // Shared modules
    implementation(project(":shared:altitude"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:error"))
    implementation(project(":shared:graphics"))
    implementation(project(":shared:id"))
    implementation(project(":shared:map"))
    implementation(project(":shared:location"))

    // Bridge modules
    implementation(project(":feature:bridge:connection-session:api"))
    implementation(project(":feature:bridge:nutrition-session:api"))
    implementation(project(":feature:bridge:profile-session:api"))
    implementation(project(":feature:theme"))

    testImplementation(testFixtures(project(":shared:design-system")))

    // Test fixtures dependencies
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.ui)
}
