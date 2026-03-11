plugins {
    id("cycling.bridge.impl")
}

android {
    namespace = "com.koflox.connectionsession.bridge.impl"
}

dependencies {
    implementation(project(":feature:bridge:connection-session:api"))
    implementation(project(":feature:connections"))
    implementation(project(":feature:sensor:power"))
}
