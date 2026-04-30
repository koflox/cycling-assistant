plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.routerender"
}

dependencies {
    // Domain types
    api(project(":feature:session:domain"))

    // Stats display strings (R class for session_stat_*)
    implementation(project(":feature:session:stats-display"))

    // Map rendering
    api(libs.maps.compose)
    api(libs.play.services.maps)
    implementation(project(":shared:map"))

    // Test fixtures
    testImplementation(testFixtures(project(":feature:session:data")))
    testImplementation(testFixtures(project(":shared:design-system")))
}
