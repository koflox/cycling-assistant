plugins {
    id("cycling.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.koflox.destinations"
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // JSON Serialization
    implementation(libs.kotlinx.serialization.json)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Bridge modules
    implementation(project(":feature:bridge:destination-nutrition:api"))
    implementation(project(":feature:bridge:destination-poi:api"))
    implementation(project(":feature:bridge:destination-session:api"))

    // Shared modules
    implementation(project(":shared:distance"))
    implementation(project(":shared:graphics"))
    implementation(project(":shared:location"))
    implementation(project(":shared:map"))
}
