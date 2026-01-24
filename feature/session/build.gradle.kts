plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.koflox.session"
    buildFeatures {
        compose = true
    }
    testFixtures {
        enable = true
        androidResources = true
    }
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "${rootProject.projectDir}/schemas/session")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ViewModels & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)

    // Room (for entity annotations - database built in :feature:database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // Shared modules
    implementation(project(":shared:altitude"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:design-system"))
    implementation(project(":shared:di"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:error"))
    implementation(project(":shared:id"))
    implementation(project(":shared:location"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.koin.test)
    testImplementation(project(":shared:testing"))

    // Test fixtures dependencies
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.ui)

    // UI Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
