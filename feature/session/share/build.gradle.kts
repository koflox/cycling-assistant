plugins {
    id("cycling.feature")
    id("cycling.testing.screenshot")
}

android {
    namespace = "com.koflox.session.share"
}

dependencies {
    // Session sub-modules
    api(project(":feature:session:domain"))
    api(project(":feature:session:route-render"))
    implementation(project(":feature:session:stats-display"))

    // Shared modules
    implementation(project(":shared:gpx"))
    implementation(project(":shared:location:domain"))

    // Strava integration
    implementation(project(":feature:integrations:strava:api"))

    // Test fixtures
    testImplementation(testFixtures(project(":feature:session:data")))
    testImplementation(testFixtures(project(":shared:design-system")))
}
