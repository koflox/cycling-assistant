@file:Suppress("TooManyFunctions")

package com.koflox.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.koflox.designsystem.testtag.TestTags

private const val DEFAULT_TIMEOUT_MS = 10_000L
private const val MAP_IDLE_TIMEOUT_MS = 5_000L
private const val DRAG_STEPS = 20

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

private fun MacrobenchmarkScope.awaitObject(resourceId: String): UiObject2 {
    device.wait(Until.hasObject(By.res(resourceId)), DEFAULT_TIMEOUT_MS)
    return device.findObject(By.res(resourceId))
        ?: error("Element '$resourceId' not found after ${DEFAULT_TIMEOUT_MS}ms. Visible package: ${device.currentPackageName}")
}

private fun MacrobenchmarkScope.waitAndClick(resourceId: String) {
    awaitObject(resourceId).click()
    device.waitForIdle()
}

fun MacrobenchmarkScope.waitForDashboard() {
    awaitObject(TestTags.MENU_BUTTON)
    device.waitForIdle(MAP_IDLE_TIMEOUT_MS)
}

fun MacrobenchmarkScope.openMenu() {
    if (!device.hasObject(By.res(TestTags.MENU_SETTINGS))) {
        waitAndClick(TestTags.MENU_BUTTON)
    }
    awaitObject(TestTags.MENU_SETTINGS)
}

fun MacrobenchmarkScope.navigateToSettingsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    awaitObject(TestTags.SETTINGS_SCREEN)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToConnectionsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_CONNECTIONS)
    awaitObject(TestTags.CONNECTIONS_SCREEN)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToSessionsAndBack() {
    openMenu()
    waitAndClick(TestTags.MENU_SESSIONS)
    awaitObject(TestTags.SESSIONS_LIST_SCREEN)
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.settingsThemeAndLanguageSelection() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    awaitObject(TestTags.SETTINGS_SCREEN)
    device.waitForIdle()

    awaitObject(TestTags.SETTINGS_THEME_DROPDOWN).click()
    device.waitForIdle()

    awaitObject(TestTags.SETTINGS_LANGUAGE_DROPDOWN).click()
    device.waitForIdle()

    device.pressBack()
    // if true, the menu button still didn't appear so the current screen is settings
    if (device.wait(Until.hasObject(By.res(TestTags.MENU_BUTTON)), DEFAULT_TIMEOUT_MS) != true) {
        device.pressBack()
    }
    waitForDashboard()
}

fun MacrobenchmarkScope.settingsStatsConfigScrolling() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    awaitObject(TestTags.SETTINGS_SCREEN)
    device.waitForIdle()

    waitAndClick(TestTags.STATS_CONFIG_NAVIGATE)
    awaitObject(TestTags.STATS_CONFIG_SCREEN)
    device.waitForIdle()

    awaitObject(TestTags.STATS_CONFIG_SCROLL).fling(Direction.DOWN)
    device.waitForIdle()

    device.pressBack()
    device.pressBack()
    waitForDashboard()
}

fun MacrobenchmarkScope.navigateToStatsConfig() {
    openMenu()
    waitAndClick(TestTags.MENU_SETTINGS)
    awaitObject(TestTags.SETTINGS_SCREEN)
    device.waitForIdle()
    waitAndClick(TestTags.STATS_CONFIG_NAVIGATE)
    awaitObject(TestTags.STATS_CONFIG_SCREEN)
    device.waitForIdle()
}

fun MacrobenchmarkScope.scrollStatsConfigBody() {
    val scroll = awaitObject(TestTags.STATS_CONFIG_SCROLL)
    scroll.fling(Direction.DOWN)
    device.waitForIdle()
    scroll.fling(Direction.UP)
    device.waitForIdle()
}

fun MacrobenchmarkScope.statsConfigDragReorder() {
    navigateToStatsConfig()

    val selectedList = awaitObject(TestTags.STATS_CONFIG_SELECTED_LIST)
    val children = selectedList.children
    if (children.size >= 2) {
        val firstHandle = children[0].findObject(By.res(TestTags.DRAG_HANDLE))
        val secondHandle = children[1].findObject(By.res(TestTags.DRAG_HANDLE))
        if (firstHandle != null && secondHandle != null) {
            val startX = firstHandle.visibleCenter.x
            val startY = firstHandle.visibleCenter.y
            val endY = secondHandle.visibleCenter.y
            device.drag(startX, startY, startX, endY, DRAG_STEPS)
            device.waitForIdle()
        }
    }

    device.pressBack()
    device.pressBack()
    waitForDashboard()
}
