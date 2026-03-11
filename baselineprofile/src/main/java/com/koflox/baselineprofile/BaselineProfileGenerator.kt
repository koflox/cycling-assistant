package com.koflox.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates Baseline Profiles for the CyclingAssistant app covering startup
 * and critical user journeys.
 *
 * Run with:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 *
 * Requires API 33+ or rooted API 28+.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Before
    fun setup() {
        grantPermissions()
    }

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = targetPackageName(),
            includeInStartupProfile = true,
        ) {
            grantPermissions()
            pressHome()
            startActivityAndWait()
            waitForDashboard()
        }
    }

    @Test
    fun generateCriticalUserJourneyProfile() {
        rule.collect(
            packageName = targetPackageName(),
            includeInStartupProfile = false,
        ) {
            grantPermissions()
            pressHome()
            startActivityAndWait()
            waitForDashboard()

            navigateToSettingsAndBack()
            navigateToConnectionsAndBack()
            navigateToSessionsAndBack()
            settingsThemeAndLanguageSelection()
            settingsStatsConfigScrolling()
            statsConfigDragReorder()
        }
    }
}
