plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.graphics"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
