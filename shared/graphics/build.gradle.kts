plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.koflox.graphics"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
