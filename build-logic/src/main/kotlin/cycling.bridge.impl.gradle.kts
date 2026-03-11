import com.koflox.convention.library
import com.koflox.convention.libs

plugins {
    id("cycling.library")
    id("cycling.hilt")
    id("cycling.testing.unit")
}

dependencies {
    "implementation"(libs.library("kotlinx-coroutines-core"))
}
