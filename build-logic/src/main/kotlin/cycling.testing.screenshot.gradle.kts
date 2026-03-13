import com.android.build.api.dsl.LibraryExtension
import com.koflox.convention.library
import com.koflox.convention.libs
import io.github.takahirom.roborazzi.RoborazziExtension
import io.github.takahirom.roborazzi.RoborazziPlugin

apply<RoborazziPlugin>()

extensions.configure<LibraryExtension> {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

extensions.configure<RoborazziExtension> {
    outputDir.set(layout.projectDirectory.dir("src/test/snapshots"))
}

dependencies {
    "testImplementation"(libs.library("roborazzi-core"))
    "testImplementation"(libs.library("roborazzi-compose"))
    "testImplementation"(libs.library("roborazzi-junit-rule"))
    "testImplementation"(libs.library("robolectric"))
    "testImplementation"(libs.library("androidx-compose-ui-test-junit4"))
    "debugImplementation"(libs.library("androidx-compose-ui-test-manifest"))
}
