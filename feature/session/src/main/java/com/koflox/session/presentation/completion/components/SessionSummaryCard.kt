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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R

private data class SummaryStyle(
    val innerPadding: Dp,
    val destinationStyle: TextStyle,
    val dateStyle: TextStyle,
    val destinationSpacer: Dp,
    val statRowTopPadding: Dp,
    val statValueStyle: TextStyle,
    val statLabelStyle: TextStyle,
    val statPairGap: Dp,
)

@Composable
private fun resolveSummaryStyle(isCompact: Boolean): SummaryStyle {
    val typography = MaterialTheme.typography
    return if (isCompact) {
        SummaryStyle(
            innerPadding = Spacing.Medium,
            destinationStyle = typography.titleMedium,
            dateStyle = typography.bodySmall,
            destinationSpacer = 2.dp,
            statRowTopPadding = Spacing.Tiny,
            statValueStyle = typography.labelLarge,
            statLabelStyle = typography.labelSmall,
            statPairGap = Spacing.Large,
        )
    } else {
        SummaryStyle(
            innerPadding = Spacing.Large,
            destinationStyle = typography.titleLarge,
            dateStyle = typography.titleMedium,
            destinationSpacer = Spacing.Tiny,
            statRowTopPadding = Spacing.Small,
            statValueStyle = typography.titleSmall,
            statLabelStyle = typography.bodySmall,
            statPairGap = Spacing.ExtraLarge,
        )
    }
}

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
    isCompact: Boolean = false,
) {
    val outerPadding = if (isCompact) Spacing.Small else Spacing.Large
    Card(
        modifier = modifier.padding(outerPadding),
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
            isCompact = isCompact,
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
    isCompact: Boolean,
) {
    val style = resolveSummaryStyle(isCompact)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(style.innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isCompact) {
            if (destinationName != null) {
                Text(
                    text = destinationName,
                    style = style.destinationStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(style.destinationSpacer))
            }
            Text(text = startDate, style = style.dateStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isCompact) {
            CompactStatGrid(
                elapsedTime = elapsedTime,
                movingTime = movingTime,
                idleTime = idleTime,
                distance = distance,
                averageSpeed = averageSpeed,
                topSpeed = topSpeed,
                altitudeGain = altitudeGain,
                altitudeLoss = altitudeLoss,
                calories = calories,
                style = style,
            )
        } else {
            SummaryStatRows(
                elapsedTime = elapsedTime,
                movingTime = movingTime,
                idleTime = idleTime,
                distance = distance,
                averageSpeed = averageSpeed,
                topSpeed = topSpeed,
                altitudeGain = altitudeGain,
                altitudeLoss = altitudeLoss,
                calories = calories,
                style = style,
            )
        }
    }
}

@Composable
private fun SummaryStatRows(
    elapsedTime: String,
    movingTime: String,
    idleTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    altitudeLoss: String,
    calories: String?,
    style: SummaryStyle,
) {
    StatRow(
        leftLabel = stringResource(R.string.session_stat_time),
        leftValue = elapsedTime,
        rightLabel = stringResource(R.string.session_stat_distance),
        rightValue = stringResource(R.string.session_stat_value_km, distance),
        topPadding = style.statRowTopPadding,
        valueStyle = style.statValueStyle,
        labelStyle = style.statLabelStyle,
        pairGap = style.statPairGap,
    )
    StatRow(
        leftLabel = stringResource(R.string.session_stat_moving_time),
        leftValue = movingTime,
        rightLabel = stringResource(R.string.session_stat_idle_time),
        rightValue = idleTime,
        topPadding = style.statRowTopPadding,
        valueStyle = style.statValueStyle,
        labelStyle = style.statLabelStyle,
        pairGap = style.statPairGap,
    )
    StatRow(
        leftLabel = stringResource(R.string.session_stat_avg_speed),
        leftValue = stringResource(R.string.session_stat_value_kmh, averageSpeed),
        rightLabel = stringResource(R.string.session_stat_top_speed),
        rightValue = stringResource(R.string.session_stat_value_kmh, topSpeed),
        topPadding = style.statRowTopPadding,
        valueStyle = style.statValueStyle,
        labelStyle = style.statLabelStyle,
        pairGap = style.statPairGap,
    )
    StatRow(
        leftLabel = stringResource(R.string.session_stat_altitude_gain),
        leftValue = stringResource(R.string.session_stat_value_m, altitudeGain),
        rightLabel = stringResource(R.string.session_stat_altitude_loss),
        rightValue = stringResource(R.string.session_stat_value_m, altitudeLoss),
        topPadding = style.statRowTopPadding,
        valueStyle = style.statValueStyle,
        labelStyle = style.statLabelStyle,
        pairGap = style.statPairGap,
    )
    if (calories != null) {
        StatRow(
            leftLabel = stringResource(R.string.session_stat_calories),
            leftValue = stringResource(R.string.session_stat_value_kcal, calories),
            topPadding = style.statRowTopPadding,
            valueStyle = style.statValueStyle,
            labelStyle = style.statLabelStyle,
            pairGap = style.statPairGap,
        )
    }
}

@Composable
private fun CompactStatGrid(
    elapsedTime: String,
    movingTime: String,
    idleTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    altitudeGain: String,
    altitudeLoss: String,
    calories: String?,
    style: SummaryStyle,
) {
    val vs = style.statValueStyle
    val ls = style.statLabelStyle
    val rowModifier = Modifier.fillMaxWidth().padding(top = style.statRowTopPadding)
    Row(modifier = rowModifier) {
        val w = Modifier.weight(1f)
        StatItem(stringResource(R.string.session_stat_time), elapsedTime, vs, ls, w)
        val distanceValue = stringResource(R.string.session_stat_value_km, distance)
        StatItem(stringResource(R.string.session_stat_distance), distanceValue, vs, ls, w)
        StatItem(stringResource(R.string.session_stat_moving_time), movingTime, vs, ls, w)
        StatItem(stringResource(R.string.session_stat_idle_time), idleTime, vs, ls, w)
    }
    Row(modifier = rowModifier) {
        val w = Modifier.weight(1f)
        val avgSpeedValue = stringResource(R.string.session_stat_value_kmh, averageSpeed)
        StatItem(stringResource(R.string.session_stat_avg_speed), avgSpeedValue, vs, ls, w)
        val topSpeedValue = stringResource(R.string.session_stat_value_kmh, topSpeed)
        StatItem(stringResource(R.string.session_stat_top_speed), topSpeedValue, vs, ls, w)
        val altGainValue = stringResource(R.string.session_stat_value_m, altitudeGain)
        StatItem(stringResource(R.string.session_stat_altitude_gain), altGainValue, vs, ls, w)
        val altLossValue = stringResource(R.string.session_stat_value_m, altitudeLoss)
        StatItem(stringResource(R.string.session_stat_altitude_loss), altLossValue, vs, ls, w)
    }
    if (calories != null) {
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.Center) {
            val caloriesValue = stringResource(R.string.session_stat_value_kcal, calories)
            StatItem(stringResource(R.string.session_stat_calories), caloriesValue, vs, ls)
        }
    }
}

@Composable
private fun StatRow(
    leftLabel: String,
    leftValue: String,
    topPadding: Dp = Spacing.Small,
    valueStyle: TextStyle = MaterialTheme.typography.titleSmall,
    labelStyle: TextStyle = MaterialTheme.typography.bodySmall,
    pairGap: Dp = Spacing.ExtraLarge,
    rightLabel: String? = null,
    rightValue: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding),
        horizontalArrangement = Arrangement.Center,
    ) {
        StatItem(label = leftLabel, value = leftValue, valueStyle = valueStyle, labelStyle = labelStyle)
        if (rightLabel != null && rightValue != null) {
            Spacer(modifier = Modifier.width(pairGap))
            StatItem(label = rightLabel, value = rightValue, valueStyle = valueStyle, labelStyle = labelStyle)
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueStyle: TextStyle = MaterialTheme.typography.titleSmall,
    labelStyle: TextStyle = MaterialTheme.typography.bodySmall,
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
