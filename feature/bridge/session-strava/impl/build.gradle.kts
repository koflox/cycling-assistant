plugins {
    id("cycling.bridge.impl")
}

android {
    namespace = "com.koflox.sessionstrava.bridge.impl"
}

dependencies {
    implementation(project(":feature:bridge:session-strava:api"))
    implementation(project(":feature:session:domain"))
    implementation(project(":feature:session:share"))
    implementation(project(":shared:gpx"))
}
