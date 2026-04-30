package com.koflox.session.presentation.statsdisplay.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.presentation.statsdisplay.StatsDisplaySection
import com.koflox.session.presentation.statsdisplay.sectionToConstraintRes
import com.koflox.session.presentation.statsdisplay.statTypeToLabelRes

@Composable
internal fun StatsPreviewCard(
    section: StatsDisplaySection,
    selectedStats: List<SessionStatType>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        if (selectedStats.isEmpty()) {
            EmptyPreviewLayout(
                section = section,
                modifier = Modifier.padding(Spacing.Medium),
            )
        } else {
            when (section) {
                StatsDisplaySection.ACTIVE_SESSION -> ActivePreviewLayout(
                    stats = selectedStats,
                    modifier = Modifier.padding(Spacing.Medium),
                )
                StatsDisplaySection.COMPLETED_SESSION,
                StatsDisplaySection.SHARE,
                -> GridPreviewLayout(
                    stats = selectedStats,
                    modifier = Modifier.padding(Spacing.Medium),
                )
            }
        }
    }
}

@Composable
private fun ActivePreviewLayout(
    stats: List<SessionStatType>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        stats.forEach { type ->
            PreviewStatItem(label = stringResource(statTypeToLabelRes(type)))
        }
    }
}

@Composable
private fun GridPreviewLayout(
    stats: List<SessionStatType>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        stats.chunked(GRID_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { type ->
                    PreviewStatItem(
                        label = stringResource(statTypeToLabelRes(type)),
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(GRID_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptyPreviewLayout(
    section: StatsDisplaySection,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = PLACEHOLDER_VALUE,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(sectionToConstraintRes(section)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreviewStatItem(
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = PLACEHOLDER_VALUE,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private const val GRID_COLUMNS = 4
private const val PLACEHOLDER_VALUE = "---"
