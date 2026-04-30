plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.completion"
}

dependencies {
    api(project(":feature:session:domain"))
    api(project(":feature:session:route-render"))
    implementation(project(":feature:session:stats-display"))

    implementation(project(":shared:error"))
    implementation(project(":shared:graphics"))
    implementation(project(":shared:location:domain"))
    implementation(project(":shared:map"))

    // Test fixtures
    testImplementation(testFixtures(project(":feature:session:data")))
}
