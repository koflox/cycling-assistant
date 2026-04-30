plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.session.navgraph"
}

dependencies {
    // Sub-features whose Routes are wired into sessionGraph
    implementation(project(":feature:session:completion"))
    implementation(project(":feature:session:history"))
    implementation(project(":feature:session:share"))
    implementation(project(":feature:session:stats-display"))
}
