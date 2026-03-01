plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.connectionsession.bridge"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
