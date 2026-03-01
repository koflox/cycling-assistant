package com.koflox.session.presentation.completion.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
    averagePower: String? = null,
    maxPower: String? = null,
    isSharePreview: Boolean = false,
) {
    Card(
        modifier = modifier.padding(Spacing.Small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
    ) {
        SessionSummaryContent(
            elapsedTime = elapsedTime,
            movingTime = movingTime,
            idleTime = idleTime,
            distance = distance,
            averageSpeed = averageSpeed,
            topSpeed = topSpeed,
            altitudeGain = altitudeGain,
            altitudeLoss = altitudeLoss,
            calories = calories,
            averagePower = averagePower,
            maxPower = maxPower,
            isSharePreview = isSharePreview,
        )
    }
}

@Composable
private fun SessionSummaryContent(
    elapsedTime: String,
    movingTime: String,
    idleTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    altitudeLoss: String,
    calories: String?,
    averagePower: String?,
    maxPower: String?,
    isSharePreview: Boolean,
) {
    val typography = MaterialTheme.typography
    val valueStyle = typography.labelLarge
    val labelStyle = typography.labelSmall
    val rowModifier = Modifier.fillMaxWidth().padding(top = Spacing.Tiny)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = rowModifier) {
            val w = Modifier.weight(1f)
            StatItem(stringResource(R.string.session_stat_time), elapsedTime, valueStyle, labelStyle, w)
            val distanceValue = stringResource(R.string.session_stat_value_km, distance)
            StatItem(stringResource(R.string.session_stat_distance), distanceValue, valueStyle, labelStyle, w)
            StatItem(stringResource(R.string.session_stat_moving_time), movingTime, valueStyle, labelStyle, w)
            if (isSharePreview && averagePower != null) {
                val avgPowerValue = stringResource(R.string.session_stat_value_w, averagePower)
                StatItem(stringResource(R.string.session_stat_avg_power), avgPowerValue, valueStyle, labelStyle, w)
            } else {
                StatItem(stringResource(R.string.session_stat_idle_time), idleTime, valueStyle, labelStyle, w)
            }
        }
        Row(modifier = rowModifier) {
            val w = Modifier.weight(1f)
            val avgSpeedValue = stringResource(R.string.session_stat_value_kmh, averageSpeed)
            StatItem(stringResource(R.string.session_stat_avg_speed), avgSpeedValue, valueStyle, labelStyle, w)
            val topSpeedValue = stringResource(R.string.session_stat_value_kmh, topSpeed)
            StatItem(stringResource(R.string.session_stat_top_speed), topSpeedValue, valueStyle, labelStyle, w)
            val altGainValue = stringResource(R.string.session_stat_value_m, altitudeGain)
            StatItem(stringResource(R.string.session_stat_altitude_gain), altGainValue, valueStyle, labelStyle, w)
            val altLossValue = stringResource(R.string.session_stat_value_m, altitudeLoss)
            StatItem(stringResource(R.string.session_stat_altitude_loss), altLossValue, valueStyle, labelStyle, w)
        }
        if (averagePower != null && maxPower != null) {
            Row(modifier = rowModifier) {
                val w = Modifier.weight(1f)
                val avgPowerValue = stringResource(R.string.session_stat_value_w, averagePower)
                StatItem(stringResource(R.string.session_stat_avg_power), avgPowerValue, valueStyle, labelStyle, w)
                val maxPowerValue = stringResource(R.string.session_stat_value_w, maxPower)
                StatItem(stringResource(R.string.session_stat_max_power), maxPowerValue, valueStyle, labelStyle, w)
                if (calories != null) {
                    val caloriesValue = stringResource(R.string.session_stat_value_kcal, calories)
                    StatItem(stringResource(R.string.session_stat_calories), caloriesValue, valueStyle, labelStyle, w)
                }
            }
        } else if (calories != null) {
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.Center) {
                val caloriesValue = stringResource(R.string.session_stat_value_kcal, calories)
                StatItem(stringResource(R.string.session_stat_calories), caloriesValue, valueStyle, labelStyle)
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueStyle: TextStyle = MaterialTheme.typography.labelLarge,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = valueStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
