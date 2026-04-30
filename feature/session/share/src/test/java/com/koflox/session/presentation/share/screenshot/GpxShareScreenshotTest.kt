package com.koflox.session.presentation.share.screenshot

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
import com.koflox.session.presentation.share.GpxShareState
import com.koflox.session.presentation.share.GpxShareTab
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class GpxShareScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `gpx share idle light`() {
        snapshot(gpxShareState = GpxShareState.Idle)
    }

    @Test
    fun `gpx share idle dark`() {
        snapshot(gpxShareState = GpxShareState.Idle, isDark = true)
    }

    @Test
    fun `gpx share generating light`() {
        snapshot(gpxShareState = GpxShareState.Generating)
    }

    @Test
    fun `gpx share unavailable light`() {
        snapshot(gpxShareState = GpxShareState.Unavailable)
    }

    private fun snapshot(gpxShareState: GpxShareState, isDark: Boolean = false) {
        composeTestRule.setContent {
            TestTheme(isDark = isDark) {
                Surface {
                    GpxShareTab(
                        gpxShareState = gpxShareState,
                        onEvent = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
