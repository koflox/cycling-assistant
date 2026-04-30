plugins {
    id("cycling.library")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

android {
    namespace = "com.koflox.session.data"

    testFixtures {
        enable = true
    }
}

dependencies {
    // Domain (pure Kotlin) — exposed via api so consumers see SessionDao/Entity
    api(project(":feature:session:domain"))

    // Shared modules
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:di"))

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Room
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Test fixtures
    testFixturesImplementation(project(":feature:session:domain"))
}
