plugins {
    id("cycling.kotlin.library")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":shared:altitude"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:id"))
    implementation(project(":shared:location:domain"))
    implementation(project(":feature:bridge:profile-session:api"))
}
