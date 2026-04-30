plugins {
    id("cycling.feature")
    id("cycling.testing.screenshot")
}

android {
    namespace = "com.koflox.session.tracking"
}

dependencies {
    // Permissions
    implementation(libs.accompanist.permissions)

    // Session sub-modules
    api(project(":feature:session:domain"))
    api(project(":feature:session:route-render"))
    implementation(project(":feature:session:stats-display"))

    // Shared modules
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:error"))
    implementation(project(":shared:location:domain"))
    implementation(project(":shared:location:data"))

    // Bridge modules
    implementation(project(":feature:bridge:connection-session:api"))
    implementation(project(":feature:bridge:nutrition-session:api"))
    implementation(project(":feature:theme"))

    // Strava integration (auto-enqueue on session save)
    implementation(project(":feature:integrations:strava:api"))

    // Test fixtures
    testImplementation(testFixtures(project(":feature:session:data")))
    testImplementation(testFixtures(project(":shared:design-system")))
}
