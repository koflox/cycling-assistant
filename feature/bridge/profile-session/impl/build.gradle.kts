plugins {
    id("cycling.bridge.impl")
}

android {
    namespace = "com.koflox.profilesession.bridge.impl"
}

dependencies {
    implementation(project(":feature:bridge:profile-session:api"))
    implementation(project(":feature:profile"))
}
