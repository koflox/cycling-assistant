plugins {
    id("cycling.bridge.impl")
}

android {
    namespace = "com.koflox.sessionstrava.bridge.impl"
}

dependencies {
    implementation(project(":feature:bridge:session-strava:api"))
    implementation(project(":feature:session"))
    implementation(project(":shared:gpx"))
}
