@file:Suppress("TooManyFunctions")

package com.koflox.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.koflox.designsystem.testtag.TestTags

private const val DEFAULT_TIMEOUT_MS = 10_000L
private const val MAP_IDLE_TIMEOUT_MS = 5_000L

fun targetPackageName(): String {
    return InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: "com.koflox.cyclingassistant"
}

fun grantPermissions() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val packageName = targetPackageName()
    device.executeShellCommand("pm grant $packageName android.permission.ACCESS_FINE_LOCATION")
    device.executeShellCommand("pm grant $packageName android.permission.ACCESS_COARSE_LOCATION")
    device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
}

private fun MacrobenchmarkScope.waitAndClick(resourceId: String) {
    device.wait(Until.hasObject(By.res(resourceId)), DEFAULT_TIMEOUT_MS)
    device.findObject(By.res(resourceId)).click()
    device.waitForIdle()
}

fun MacrobenchmarkScope.waitForDashboard() {
    device.wait(Until.hasObject(By.res(TestTags.MENU_BUTTON)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle(MAP_IDLE_TIMEOUT_MS)
}

fun MacrobenchmarkScope.openMenu() {
    waitAndClick(TestTags.MENU_BUTTON)
}

fun MacrobenchmarkScope.navigateToSettingsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    device.wait(Until.hasObject(By.res(TestTags.SETTINGS_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToConnectionsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_CONNECTIONS)
    device.wait(Until.hasObject(By.res(TestTags.CONNECTIONS_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToSessionsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_SESSIONS)
    device.wait(Until.hasObject(By.res(TestTags.SESSIONS_LIST_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.settingsThemeAndLanguageSelection() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    device.wait(Until.hasObject(By.res(TestTags.SETTINGS_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle()

    waitAndClick(TestTags.SETTINGS_THEME_DROPDOWN)
    device.pressBack()
    device.waitForIdle()

    waitAndClick(TestTags.SETTINGS_LANGUAGE_DROPDOWN)
    device.pressBack()
    device.waitForIdle()

    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.settingsStatsConfigScrolling() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    device.wait(Until.hasObject(By.res(TestTags.SETTINGS_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle()

    waitAndClick(TestTags.STATS_CONFIG_NAVIGATE)
    device.wait(Until.hasObject(By.res(TestTags.STATS_CONFIG_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle()

    device.wait(Until.hasObject(By.res(TestTags.STATS_CONFIG_SCROLL)), DEFAULT_TIMEOUT_MS)
    device.findObject(By.res(TestTags.STATS_CONFIG_SCROLL))?.fling(Direction.DOWN)
    device.waitForIdle()

    device.pressBack()
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToStatsConfig() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    device.wait(Until.hasObject(By.res(TestTags.SETTINGS_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle()
    waitAndClick(TestTags.STATS_CONFIG_NAVIGATE)
    device.wait(Until.hasObject(By.res(TestTags.STATS_CONFIG_SCREEN)), DEFAULT_TIMEOUT_MS)
    device.waitForIdle()
}

fun MacrobenchmarkScope.scrollStatsConfigBody() {
    device.wait(Until.hasObject(By.res(TestTags.STATS_CONFIG_SCROLL)), DEFAULT_TIMEOUT_MS)
    val scroll = device.findObject(By.res(TestTags.STATS_CONFIG_SCROLL))
    scroll?.fling(Direction.DOWN)
    device.waitForIdle()
    scroll?.fling(Direction.UP)
    device.waitForIdle()
}
