import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf.plugin)
    alias(libs.plugins.hilt)
}

val versionProperties = Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

android {
    namespace = "com.koflox.cyclingassistant"

    defaultConfig {
        applicationId = "com.koflox.cyclingassistant"
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = versionProperties.getProperty("versionCode").toInt()
        versionName = versionProperties.getProperty("versionName")

        val secretsProperties = Properties()
        val secretsPropertiesFile = project.rootProject.file("secrets.properties")
        if (secretsPropertiesFile.exists()) {
            secretsProperties.load(secretsPropertiesFile.inputStream())
        }

        manifestPlaceholders["MAPS_API_KEY"] = secretsProperties.getProperty("MAPS_API_KEY", "")
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = project.rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties().apply {
                    load(keystorePropertiesFile.inputStream())
                }
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            } else {
                storeFile = file(System.getenv("RELEASE_KEYSTORE_PATH") ?: "release.jks")
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
            manifestPlaceholders["observabilityCollectionEnabled"] = "false"
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = ".staging"
            matchingFallbacks += "debug"
            manifestPlaceholders["observabilityCollectionEnabled"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["observabilityCollectionEnabled"] = "true"
        }
    }

    bundle {
        language {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "${rootProject.projectDir}/schemas/app")
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

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room - database defined in app module for KSP visibility
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SQLCipher - Room database encryption
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)

    // Feature modules
    implementation(project(":feature:connections"))
    implementation(project(":feature:bridge:connection-session:api"))
    implementation(project(":feature:bridge:connection-session:impl"))
    implementation(project(":feature:bridge:destination-nutrition:api"))
    implementation(project(":feature:bridge:destination-nutrition:impl"))
    implementation(project(":feature:bridge:destination-poi:api"))
    implementation(project(":feature:bridge:destination-poi:impl"))
    implementation(project(":feature:bridge:destination-session:api"))
    implementation(project(":feature:bridge:destination-session:impl"))
    implementation(project(":feature:bridge:nutrition-session:api"))
    implementation(project(":feature:bridge:nutrition-session:impl"))
    implementation(project(":feature:bridge:nutrition-settings:api"))
    implementation(project(":feature:bridge:nutrition-settings:impl"))
    implementation(project(":feature:bridge:poi-settings:api"))
    implementation(project(":feature:bridge:poi-settings:impl"))
    implementation(project(":feature:bridge:profile-session:api"))
    implementation(project(":feature:bridge:profile-session:impl"))
    implementation(project(":feature:bridge:session-settings:api"))
    implementation(project(":feature:bridge:session-settings:impl"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:destinations"))
    implementation(project(":feature:locale"))
    implementation(project(":feature:nutrition"))
    implementation(project(":feature:poi"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:sensor:power"))
    implementation(project(":feature:session"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:theme"))

    // Shared modules
    implementation(project(":shared:altitude"))
    implementation(project(":shared:ble"))
    implementation(project(":shared:concurrent"))
    implementation(project(":shared:design-system"))
    implementation(project(":shared:di"))
    implementation(project(":shared:distance"))
    implementation(project(":shared:id"))
    implementation(project(":shared:location"))
    implementation(project(":shared:error"))
    implementation(project(":shared:map"))
    implementation(project(":shared:observability"))
    implementation(project(":shared:sensor-protocol"))

    // Testing
    testImplementation(project(":shared:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.hilt.android.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
