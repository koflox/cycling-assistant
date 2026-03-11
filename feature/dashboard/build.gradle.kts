plugins {
    id("cycling.library")
    id("cycling.compose")
}

android {
    namespace = "com.koflox.dashboard"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // ViewModels & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Feature modules
    implementation(project(":feature:destinations"))
    implementation(project(":feature:bridge:destination-session:api"))

    // Shared modules
    implementation(project(":shared:design-system"))
}
