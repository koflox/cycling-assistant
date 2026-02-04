plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.nutritionsession.bridge.impl"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    implementation(project(":feature:nutrition-session:bridge:api"))
    implementation(project(":feature:session"))
}
