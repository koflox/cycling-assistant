plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.init"
}

dependencies {
    // Hilt providers expose UseCases from these
    api(project(":feature:session:domain"))
    implementation(project(":feature:session:data"))

    // Domain UseCase deps
    implementation(project(":shared:altitude"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:error"))
    implementation(project(":shared:id"))
    implementation(project(":shared:location:domain"))
    implementation(project(":shared:location:data"))
    implementation(project(":feature:bridge:profile-session:api"))

    // Test fixtures (domain factories)
    testImplementation(testFixtures(project(":feature:session:data")))
    testImplementation(testFixtures(project(":shared:design-system")))
}
