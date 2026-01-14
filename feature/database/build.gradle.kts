plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.koflox.database"

    defaultConfig {
        ksp {
            arg("room.schemaLocation", "${rootProject.projectDir}/schemas/database")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)

    // Feature modules for entities/DAOs
    api(project(":feature:destinations"))
    api(project(":feature:session"))

    // Testing
    testImplementation(libs.junit)
}
