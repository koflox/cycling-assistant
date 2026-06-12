import com.koflox.convention.library
import com.koflox.convention.libs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

// Kotlin 2.4.0 defaults the compiler module name to "{group}:{project}". The colon is illegal in the
// identifiers and file names Hilt generates for `internal` declarations, so KSP fails to write them
// (NoSuchFileException, especially on Windows where ':' is not a valid path char). Pin a colon-free,
// per-module name instead. See google/ksp#2964.
val safeModuleName = project.path.drop(1).replace(':', '_')
tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        moduleName.set(safeModuleName)
    }
}

dependencies {
    "implementation"(libs.library("hilt-android"))
    "ksp"(libs.library("hilt-compiler"))
}
