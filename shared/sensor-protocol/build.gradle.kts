plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.koflox.sensorprotocol"
}

dependencies {
    testImplementation(libs.junit)
}
