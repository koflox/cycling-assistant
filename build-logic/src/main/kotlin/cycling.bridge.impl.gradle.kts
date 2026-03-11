import com.koflox.build.library
import com.koflox.build.libs

plugins {
    id("cycling.library")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

dependencies {
    "implementation"(libs.library("kotlinx-coroutines-core"))
}
