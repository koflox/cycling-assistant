plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.history"
}

dependencies {
    api(project(":feature:session:domain"))
    implementation(project(":feature:session:stats-display"))

    // Test fixtures
    testImplementation(testFixtures(project(":feature:session:data")))
    testImplementation(testFixtures(project(":shared:design-system")))
}
