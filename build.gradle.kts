// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.android.library) apply false
}

dependencies {
    detektPlugins(libs.detekt)
}

tasks.register("detektRun", io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Runs detekt analysis over whole code base"
    parallel = true
    autoCorrect = true
    config.setFrom(files("$rootDir/.lint/detekt/detekt-config.yml"))
    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/resources/**")
    reports {
        xml.required.set(true)
        html.required.set(false)
        txt.required.set(false)
    }
}
