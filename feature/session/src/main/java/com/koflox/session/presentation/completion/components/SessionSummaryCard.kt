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
    distance: String,
    averageSpeed: String,
    topSpeed: String,
    modifier: Modifier = Modifier,
    destinationName: String? = null,
) {
    Card(
        modifier = modifier.padding(Spacing.Large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
    ) {
        Column(
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
                )
                Spacer(modifier = Modifier.height(Spacing.Tiny))
            }
            Text(
                text = startDate,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                StatItem(label = stringResource(R.string.session_stat_time), value = elapsedTime)
                Spacer(modifier = Modifier.width(Spacing.ExtraLarge))
                StatItem(label = stringResource(R.string.session_stat_distance), value = "$distance km")
            }
            Spacer(modifier = Modifier.height(Spacing.Small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                StatItem(label = stringResource(R.string.session_stat_avg_speed), value = "$averageSpeed km/h")
                Spacer(modifier = Modifier.width(Spacing.ExtraLarge))
                StatItem(label = stringResource(R.string.session_stat_top_speed), value = "$topSpeed km/h")
            }
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
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
