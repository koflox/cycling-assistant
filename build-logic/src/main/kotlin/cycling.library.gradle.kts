import com.android.build.api.dsl.LibraryExtension
import com.koflox.build.libs
import com.koflox.build.version

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlinx.kover")
}

extensions.configure<LibraryExtension> {
    val javaVersion = JavaVersion.toVersion(libs.version("javaVersion"))
    compileSdk = libs.version("compileSdk").toInt()

    defaultConfig {
        minSdk = libs.version("minSdk").toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}
