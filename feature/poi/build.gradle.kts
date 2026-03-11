plugins {
    id("cycling.feature")
}

android {
    namespace = "com.koflox.poi"
}

dependencies {
    // DataStore
    implementation(libs.androidx.datastore.preferences)
}
