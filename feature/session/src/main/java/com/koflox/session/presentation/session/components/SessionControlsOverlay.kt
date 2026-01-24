package com.koflox.session.presentation.session.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(Spacing.Large),
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
                text = state.destinationName,
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
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
                onStopClick = onStopClick,
            )
        }
    }
}

@Composable
private fun SessionControlButtons(
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        if (isPaused) {
            Button(onClick = onResumeClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.session_button_continue))
            }
        } else {
            OutlinedButton(onClick = onPauseClick, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.session_button_pause))
            }
        }
        Button(
            onClick = onStopClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.session_button_stop))
        }
    }
}
