package com.koflox.destinations.presentation.destinations.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.RidingMode

@Composable
internal fun RidingModeToggle(
    selectedMode: RidingMode,
    onModeSelected: (RidingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(CornerRadius.Medium)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Standard)),
    ) {
        ModeOption(
            label = stringResource(R.string.ride_mode_free_roam),
            isSelected = selectedMode == RidingMode.FREE_ROAM,
            onClick = { onModeSelected(RidingMode.FREE_ROAM) },
        )
        ModeOption(
            label = stringResource(R.string.ride_mode_destination),
            isSelected = selectedMode == RidingMode.DESTINATION,
            onClick = { onModeSelected(RidingMode.DESTINATION) },
        )
    }
}

@Composable
private fun ModeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.Medium))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
    )
}
