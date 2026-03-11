import com.koflox.build.library
import com.koflox.build.libs

dependencies {
    "testImplementation"(libs.library("junit"))
    "testImplementation"(libs.library("kotlinx-coroutines-test"))
    "testImplementation"(libs.library("mockk"))
    "testImplementation"(libs.library("turbine"))
    "testImplementation"(project(":shared:testing"))
}
