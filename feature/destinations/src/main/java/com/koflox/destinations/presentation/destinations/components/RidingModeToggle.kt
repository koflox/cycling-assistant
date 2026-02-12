package com.koflox.destinations.presentation.destinations.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.RidingMode

@Composable
internal fun RidingModeToggle(
    selectedMode: RidingMode,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onModeSelected: (RidingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val otherModes = remember(selectedMode) { RidingMode.entries.filter { it != selectedMode } }
    Column(modifier = modifier.width(IntrinsicSize.Max)) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.overlayAbove(),
        ) {
            Column(
                modifier = Modifier.padding(bottom = Spacing.Tiny),
                verticalArrangement = Arrangement.spacedBy(Spacing.Tiny),
            ) {
                otherModes.forEach { mode ->
                    ModeOption(
                        label = stringResource(mode.labelRes()),
                        onClick = { onModeSelected(mode) },
                    )
                }
            }
        }
        SelectedModeHeader(
            label = stringResource(selectedMode.labelRes()),
            isExpanded = isExpanded,
            onClick = onToggleExpand,
        )
    }
}

@Composable
private fun SelectedModeHeader(
    label: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    val shape = RoundedCornerShape(CornerRadius.Medium)
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Standard))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = Spacing.Small),
        )
    }
}

@Composable
private fun ModeOption(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(CornerRadius.Medium)
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Standard))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Small, vertical = Spacing.Small),
    )
}

private fun Modifier.overlayAbove(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, 0) {
        placeable.place(0, -placeable.height)
    }
}

@StringRes
private fun RidingMode.labelRes(): Int = when (this) {
    RidingMode.FREE_ROAM -> R.string.ride_mode_free_roam
    RidingMode.DESTINATION -> R.string.ride_mode_destination
}
