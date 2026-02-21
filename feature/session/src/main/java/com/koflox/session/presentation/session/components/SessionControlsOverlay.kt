package com.koflox.session.presentation.session.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.session.R
import com.koflox.session.presentation.session.SessionUiState

@Composable
internal fun SessionControlsOverlay(
    state: SessionUiState.Active,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onEnableLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Standard),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.destinationName ?: stringResource(R.string.session_free_roam_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))
            SessionStatsDisplay(
                elapsedTime = state.elapsedTimeFormatted,
                distance = state.traveledDistanceFormatted,
                averageSpeed = state.averageSpeedFormatted,
                topSpeed = state.topSpeedFormatted,
                altitudeGain = state.altitudeGainFormatted,
            )
            Spacer(modifier = Modifier.height(Spacing.Large))
            SessionControlButtons(
                isPaused = state.isPaused,
                isLocationDisabled = state.isLocationDisabled,
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
                onStopClick = onStopClick,
                onEnableLocationClick = onEnableLocationClick,
            )
        }
    }
}

@Composable
private fun SessionControlButtons(
    isPaused: Boolean,
    isLocationDisabled: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onEnableLocationClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        when {
            isPaused && isLocationDisabled -> {
                DebouncedButton(onClick = onEnableLocationClick, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.location_disabled_enable))
                }
            }
            isPaused -> {
                DebouncedButton(onClick = onResumeClick, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.session_button_continue))
                }
            }
            else -> {
                DebouncedOutlinedButton(onClick = onPauseClick, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.session_button_pause))
                }
            }
        }
        DebouncedButton(
            onClick = onStopClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.session_button_stop))
        }
    }
}
