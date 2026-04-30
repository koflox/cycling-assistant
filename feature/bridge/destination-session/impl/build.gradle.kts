plugins {
    id("cycling.bridge.impl")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.destinationsession.bridge.impl"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.play.services.maps)

    implementation(project(":feature:bridge:destination-session:api"))
    implementation(project(":feature:session:domain"))
    implementation(project(":feature:session:route-render"))
    implementation(project(":feature:session:tracking"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:location:domain"))

    testImplementation(testFixtures(project(":feature:session:data")))
}
