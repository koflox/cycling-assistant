plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.nutritionsession.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
