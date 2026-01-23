package com.koflox.session.presentation.session.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.koflox.session.R

@Composable
fun SessionStatsDisplay(
    elapsedTime: String,
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    modifier: Modifier = Modifier,
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
            value = "$distance km",
        )
        StatItem(
            label = stringResource(R.string.session_stat_avg_speed),
            value = "$averageSpeed km/h",
        )
        StatItem(
            label = stringResource(R.string.session_stat_top_speed),
            value = "$topSpeed km/h",
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
