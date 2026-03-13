plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.composePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
    // implementation: Roborazzi plugin JAR lacks META-INF/gradle-plugins descriptors,
    // so Gradle can't resolve it via pluginManagement at runtime (unlike AGP/Kotlin/Hilt).
    // apply<RoborazziPlugin>() requires the class on runtime classpath.
    implementation(libs.roborazzi.gradlePlugin)
}
