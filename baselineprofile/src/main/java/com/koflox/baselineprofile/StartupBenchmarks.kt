package com.koflox.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures app startup latency with and without Baseline Profiles.
 *
 * Run on a physical device:
 * ```
 * ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.koflox.baselineprofile.StartupBenchmarks
 * ```
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

    companion object {
        private const val ITERATIONS = 10
    }

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun setup() {
        grantPermissions()
    }

    @Test
    fun startupColdNoCompilation() = benchmark(CompilationMode.None(), StartupMode.COLD)

    @Test
    fun startupColdBaselineProfiles() = benchmark(CompilationMode.Partial(BaselineProfileMode.Require), StartupMode.COLD)

    @Test
    fun startupWarmBaselineProfiles() = benchmark(CompilationMode.Partial(BaselineProfileMode.Require), StartupMode.WARM)

    @Test
    fun startupHotBaselineProfiles() = benchmark(CompilationMode.Partial(BaselineProfileMode.Require), StartupMode.HOT)

    private fun benchmark(compilationMode: CompilationMode, startupMode: StartupMode) {
        rule.measureRepeated(
            packageName = targetPackageName(),
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = startupMode,
            iterations = ITERATIONS,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()
                waitForDashboard()
            },
        )
    }
}
