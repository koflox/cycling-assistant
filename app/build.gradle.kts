import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.koflox.cyclingassistant"

    defaultConfig {
        applicationId = "com.koflox.cyclingassistant"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val secretsProperties = Properties()
        val secretsPropertiesFile = rootProject.file("secrets.properties")
        if (secretsPropertiesFile.exists()) {
            secretsProperties.load(secretsPropertiesFile.inputStream())
        }

        manifestPlaceholders["MAPS_API_KEY"] = secretsProperties.getProperty("MAPS_API_KEY", "")

        ksp {
            arg("room.schemaLocation", "${rootProject.projectDir}/schemas/app")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ViewModels & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Concurrency
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)

    // Room - database defined in app module for KSP visibility
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Feature modules
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:destinations"))
    implementation(project(":feature:destination-session:bridge:api"))
    implementation(project(":feature:destination-session:bridge:impl"))
    implementation(project(":feature:session"))
    implementation(project(":feature:session-settings:bridge:api"))
    implementation(project(":feature:session-settings:bridge:impl"))
    implementation(project(":feature:settings"))

    // Shared modules
    implementation(project(":shared:altitude"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:design-system"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:id"))
    implementation(project(":shared:location"))
    implementation(project(":shared:error"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.koin.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
