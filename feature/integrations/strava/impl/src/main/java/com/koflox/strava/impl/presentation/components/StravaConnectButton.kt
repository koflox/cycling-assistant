package com.koflox.strava.impl.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.koflox.strava.impl.R
import kotlin.time.Duration.Companion.milliseconds

private val DEBOUNCE_INTERVAL = 400.milliseconds

/**
 * Strava-branded "Connect with Strava" button. Renders the official Strava button drawable
 * directly as a clickable [Image] (no Material `Button` wrapper) so the rounded shape and
 * orange fill come from Strava's own asset — required by their brand guidelines and avoids
 * the visual gap a Material `Button`'s padding adds around the artwork.
 */
@Composable
internal fun StravaConnectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lastClickTimeMs by remember { mutableLongStateOf(0L) }
    Image(
        painter = painterResource(R.drawable.ic_strava_btn_connect),
        contentDescription = stringResource(R.string.strava_connect_button_content_description),
        modifier = modifier.clickable {
            val now = System.currentTimeMillis()
            if (now - lastClickTimeMs >= DEBOUNCE_INTERVAL.inWholeMilliseconds) {
                lastClickTimeMs = now
                onClick()
            }
        },
    )
}
