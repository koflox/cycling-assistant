package com.koflox.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures frame timing during scroll interactions in Stats Display Config.
 *
 * Run on a physical device:
 * ```
 * ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.koflox.baselineprofile.ScrollBenchmarks
 * ```
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmarks {

    companion object {
        private const val ITERATIONS = 5
    }

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun setup() {
        grantPermissions()
    }

    @Test
    fun scrollStatsConfig() {
        rule.measureRepeated(
            packageName = targetPackageName(),
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
            startupMode = StartupMode.WARM,
            iterations = ITERATIONS,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                waitForDashboard()
                navigateToStatsConfig()
            },
            measureBlock = {
                scrollStatsConfigBody()
            },
        )
    }
}
