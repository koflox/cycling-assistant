import com.koflox.convention.library
import com.koflox.convention.libs

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    "implementation"(libs.library("hilt-android"))
    "ksp"(libs.library("hilt-compiler"))
}
