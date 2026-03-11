import com.koflox.build.library
import com.koflox.build.libs

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    "implementation"(libs.library("hilt-android"))
    "ksp"(libs.library("hilt-compiler"))
}
