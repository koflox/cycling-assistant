plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.koflox.nutritionsession.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
