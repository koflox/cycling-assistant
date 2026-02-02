package com.koflox.session.presentation.completion.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R

@Composable
internal fun SessionSummaryCard(
    startDate: String,
    elapsedTime: String,
    movingTime: String,
    idleTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    altitudeLoss: String,
    calories: String?,
    modifier: Modifier = Modifier,
    destinationName: String? = null,
) {
    Card(
        modifier = modifier.padding(Spacing.Large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
    ) {
        SessionSummaryContent(
            startDate = startDate,
            elapsedTime = elapsedTime,
            movingTime = movingTime,
            idleTime = idleTime,
            distance = distance,
            averageSpeed = averageSpeed,
            topSpeed = topSpeed,
            altitudeGain = altitudeGain,
            altitudeLoss = altitudeLoss,
            calories = calories,
            destinationName = destinationName,
        )
    }
}

@Composable
private fun SessionSummaryContent(
    startDate: String,
    elapsedTime: String,
    movingTime: String,
    idleTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    altitudeLoss: String,
    calories: String?,
    destinationName: String?,
) {
    Column(
        // TODO №1: use a recycler view alternative
        // TODO №2: localize strings
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (destinationName != null) {
            Text(
                text = destinationName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.Tiny))
        }
        Text(
            text = startDate,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatRow(
            leftLabel = stringResource(R.string.session_stat_time),
            leftValue = elapsedTime,
            rightLabel = stringResource(R.string.session_stat_distance),
            rightValue = "$distance km",
        )
        StatRow(
            leftLabel = stringResource(R.string.session_stat_moving_time),
            leftValue = movingTime,
            rightLabel = stringResource(R.string.session_stat_idle_time),
            rightValue = idleTime,
        )
        StatRow(
            leftLabel = stringResource(R.string.session_stat_avg_speed),
            leftValue = "$averageSpeed km/h",
            rightLabel = stringResource(R.string.session_stat_top_speed),
            rightValue = "$topSpeed km/h",
        )
        StatRow(
            leftLabel = stringResource(R.string.session_stat_altitude_gain),
            leftValue = "$altitudeGain m",
            rightLabel = stringResource(R.string.session_stat_altitude_loss),
            rightValue = "$altitudeLoss m",
        )
        if (calories != null) {
            StatRow(
                leftLabel = stringResource(R.string.session_stat_calories),
                leftValue = "$calories kcal",
            )
        }
    }
}

@Composable
private fun StatRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String? = null,
    rightValue: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.Small),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatItem(label = leftLabel, value = leftValue)
        if (rightLabel != null && rightValue != null) {
            Spacer(modifier = Modifier.width(Spacing.ExtraLarge))
            StatItem(label = rightLabel, value = rightValue)
        }
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
