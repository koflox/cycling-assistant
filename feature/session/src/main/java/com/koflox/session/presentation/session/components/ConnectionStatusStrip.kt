package com.koflox.session.presentation.session.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.presentation.session.DeviceStripItem
import com.koflox.session.presentation.session.DeviceStripState

private val ICON_SIZE = 16.dp
private const val PULSE_INITIAL_ALPHA = 1f
private const val PULSE_TARGET_ALPHA = 0.3f
private const val PULSE_DURATION_MS = 800

@Composable
internal fun ConnectionStatusStrip(
    items: List<DeviceStripItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    items.forEach { item ->
        DeviceStatusRow(
            item = item,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DeviceStatusRow(
    item: DeviceStripItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusText = formatStatusText(item.state)
    val iconTint = when (item.state) {
        is DeviceStripState.Connected -> MaterialTheme.colorScheme.primary
        is DeviceStripState.Connecting -> MaterialTheme.colorScheme.primary
        is DeviceStripState.Reconnecting -> MaterialTheme.colorScheme.error
    }
    val isAnimated = item.state is DeviceStripState.Connecting
    val iconAlpha = if (isAnimated) {
        val transition = rememberInfiniteTransition(label = "deviceConnecting")
        val alpha by transition.animateFloat(
            initialValue = PULSE_INITIAL_ALPHA,
            targetValue = PULSE_TARGET_ALPHA,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = PULSE_DURATION_MS),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "deviceConnectingAlpha",
        )
        alpha
    } else {
        PULSE_INITIAL_ALPHA
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.Tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(iconAlpha),
        )
        Spacer(modifier = Modifier.width(Spacing.Small))
        Text(
            text = "${item.deviceName} · $statusText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun formatStatusText(state: DeviceStripState): String = when (state) {
    is DeviceStripState.Connected -> stringResource(R.string.session_stat_value_w, state.instantaneousPowerWatts.toString())
    DeviceStripState.Connecting -> stringResource(R.string.session_device_connecting)
    is DeviceStripState.Reconnecting -> stringResource(R.string.session_device_retry_countdown, state.remaining.inWholeSeconds.toInt())
}
