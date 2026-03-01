package com.koflox.session.presentation.session.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import com.koflox.session.R
import com.koflox.session.presentation.session.PowerDisplayState

@Composable
internal fun SessionStatsDisplay(
    elapsedTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    modifier: Modifier = Modifier,
    powerDisplayState: PowerDisplayState = PowerDisplayState.None,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(
            label = stringResource(R.string.session_stat_time),
            value = elapsedTime,
        )
        StatItem(
            label = stringResource(R.string.session_stat_distance),
            value = stringResource(R.string.session_stat_value_km, distance),
        )
        StatItem(
            label = stringResource(R.string.session_stat_avg_speed),
            value = stringResource(R.string.session_stat_value_kmh, averageSpeed),
        )
        when (powerDisplayState) {
            PowerDisplayState.None -> {
                StatItem(
                    label = stringResource(R.string.session_stat_top_speed),
                    value = stringResource(R.string.session_stat_value_kmh, topSpeed),
                )
            }
            PowerDisplayState.Connecting -> {
                PowerConnectingItem()
            }
            is PowerDisplayState.Receiving -> {
                StatItem(
                    label = stringResource(R.string.session_stat_power),
                    value = stringResource(R.string.session_stat_value_w, powerDisplayState.avgPowerFormatted),
                )
            }
        }
        StatItem(
            label = stringResource(R.string.session_stat_altitude_gain),
            value = stringResource(R.string.session_stat_value_m, altitudeGain),
        )
    }
}

@Composable
private fun PowerConnectingItem(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "powerConnecting")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "powerConnectingAlpha",
    )
    Column(
        modifier = modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = stringResource(R.string.session_stat_power_connecting),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.session_stat_power),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
