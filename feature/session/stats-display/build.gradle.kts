plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.statsdisplay"
}

dependencies {
    api(project(":feature:session:domain"))
}
