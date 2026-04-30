import com.koflox.convention.libs
import com.koflox.convention.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    val javaVersion = JavaVersion.toVersion(libs.version("javaVersion"))
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.version("javaVersion")))
    }
}
