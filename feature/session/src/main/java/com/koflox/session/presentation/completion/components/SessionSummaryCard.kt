package com.koflox.session.presentation.completion.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Grid
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.presentation.model.DisplayStat

@Composable
internal fun SessionSummaryCard(
    stats: List<DisplayStat>,
    modifier: Modifier = Modifier,
    title: String? = null,
    onEditClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.padding(Spacing.Small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (title != null || onEditClick != null) {
                TitleRow(title = title, onEditClick = onEditClick)
            }
            DynamicStatsGrid(stats = stats)
        }
    }
}

@Composable
private fun TitleRow(
    title: String?,
    onEditClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.Small),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (onEditClick != null) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.stats_config_edit_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DynamicStatsGrid(
    stats: List<DisplayStat>,
    modifier: Modifier = Modifier,
) {
    val valueStyle = MaterialTheme.typography.labelLarge
    val labelStyle = MaterialTheme.typography.labelSmall
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Tiny),
    ) {
        stats.chunked(Grid.StatsPerRow).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { stat ->
                    StatItem(
                        label = stat.label,
                        value = stat.value,
                        valueStyle = valueStyle,
                        labelStyle = labelStyle,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(Grid.StatsPerRow - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
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
