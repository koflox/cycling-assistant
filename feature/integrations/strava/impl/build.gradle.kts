import java.util.Properties

plugins {
    id("cycling.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.koflox.strava.impl"

    defaultConfig {
        val secretsProperties = Properties()
        val secretsPropertiesFile = project.rootProject.file("secrets.properties")
        if (secretsPropertiesFile.exists()) {
            secretsProperties.load(secretsPropertiesFile.inputStream())
        }
        buildConfigField(
            "String",
            "STRAVA_CLIENT_ID",
            "\"${secretsProperties.getProperty("STRAVA_CLIENT_ID", "")}\"",
        )
        buildConfigField(
            "String",
            "STRAVA_CLIENT_SECRET",
            "\"${secretsProperties.getProperty("STRAVA_CLIENT_SECRET", "")}\"",
        )
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.kotlinx.serialization.json)

    // Custom Tabs
    implementation(libs.androidx.browser)

    // ComponentActivity for Hilt @AndroidEntryPoint redirect activity
    implementation(libs.androidx.activity.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // GPX generator
    implementation(project(":shared:gpx"))

    // Strava API surface
    implementation(project(":feature:integrations:strava:api"))

    // Session bridge (for GPX data lookup by sessionId)
    implementation(project(":feature:bridge:session-strava:api"))

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.androidx.work.testing)
}
