package com.koflox.session.presentation.session.screenshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.koflox.designsystem.theme.CyclingDarkColorScheme
import com.koflox.designsystem.theme.CyclingLightColorScheme
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.session.DeviceStripItem
import com.koflox.session.presentation.session.DeviceStripState
import com.koflox.session.presentation.session.SessionUiState
import com.koflox.session.presentation.session.components.SessionControlsOverlay
import com.koflox.session.testutil.createActiveSessionState
import com.koflox.session.testutil.createConnectedDevice
import com.koflox.session.testutil.createReconnectingDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class SessionControlsOverlayScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `running free roam`() {
        snapshot(state = createActiveSessionState())
    }

    @Test
    fun `running with destination`() {
        snapshot(
            state = createActiveSessionState(
                destinationName = "Mount Fuji Cycling Route",
            ),
        )
    }

    @Test
    fun paused() {
        snapshot(
            state = createActiveSessionState(
                status = SessionStatus.PAUSED,
            ),
        )
    }

    @Test
    fun `paused location disabled`() {
        snapshot(
            state = createActiveSessionState(
                status = SessionStatus.PAUSED,
                isLocationDisabled = true,
            ),
        )
    }

    @Test
    fun `running with connected device`() {
        snapshot(
            state = createActiveSessionState(
                deviceStripItems = listOf(createConnectedDevice()),
            ),
        )
    }

    @Test
    fun `running with reconnecting device`() {
        snapshot(
            state = createActiveSessionState(
                deviceStripItems = listOf(createReconnectingDevice()),
            ),
        )
    }

    @Test
    fun `running with multiple devices`() {
        snapshot(
            state = createActiveSessionState(
                deviceStripItems = listOf(
                    createConnectedDevice(deviceName = "Favero Assioma", powerWatts = 245),
                    DeviceStripItem(
                        deviceName = "Wahoo KICKR",
                        state = DeviceStripState.Connecting,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `dark theme running free roam`() {
        snapshot(state = createActiveSessionState(), isDark = true)
    }

    @Test
    fun `dark theme paused with device`() {
        snapshot(
            state = createActiveSessionState(
                status = SessionStatus.PAUSED,
                deviceStripItems = listOf(createConnectedDevice()),
            ),
            isDark = true,
        )
    }

    private fun snapshot(state: SessionUiState.Active, isDark: Boolean = false) {
        composeTestRule.setContent {
            TestTheme(isDark = isDark) {
                TestOverlay(state = state)
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

@Composable
private fun TestTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDark) CyclingDarkColorScheme else CyclingLightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
private fun TestOverlay(state: SessionUiState.Active) {
    Surface {
        SessionControlsOverlay(
            state = state,
            onPauseClick = {},
            onResumeClick = {},
            onStopClick = {},
            onEnableLocationClick = {},
            onDeviceStripClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
