import com.koflox.convention.library
import com.koflox.convention.libs

dependencies {
    "testImplementation"(libs.library("junit"))
    "testImplementation"(libs.library("kotlinx-coroutines-test"))
    "testImplementation"(libs.library("mockk"))
    "testImplementation"(libs.library("turbine"))
    "testImplementation"(project(":shared:testing"))
}
