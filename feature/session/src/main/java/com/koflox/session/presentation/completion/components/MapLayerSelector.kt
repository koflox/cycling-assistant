package com.koflox.session.presentation.completion.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.component.AnchorStartPopupMenu
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.session.R
import com.koflox.session.presentation.route.MapLayer

@Composable
internal fun MapLayerSelector(
    selectedLayer: MapLayer,
    availableLayers: List<MapLayer>,
    onLayerSelected: (MapLayer) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Light),
        shadowElevation = CornerRadius.Small,
    ) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = selectedLayer.toIcon(),
                contentDescription = stringResource(R.string.map_layer_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        AnchorStartPopupMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.padding(end = Spacing.Small),
        ) {
            availableLayers.forEach { layer ->
                LayerMenuItem(
                    layer = layer,
                    isSelected = layer == selectedLayer,
                    onClick = {
                        isExpanded = false
                        onLayerSelected(layer)
                    },
                )
            }
        }
    }
}

private val LAYER_MENU_ICON_SIZE = 24.dp

@Composable
private fun LayerMenuItem(
    layer: MapLayer,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
    ) {
        Icon(
            imageVector = layer.toIcon(),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(LAYER_MENU_ICON_SIZE),
        )
        Text(
            text = layer.toLabel(),
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            modifier = Modifier.padding(start = Spacing.Small),
        )
    }
}

private fun MapLayer.toIcon(): ImageVector = when (this) {
    MapLayer.DEFAULT -> Icons.Outlined.Route
    MapLayer.SPEED -> Icons.Outlined.Speed
    MapLayer.POWER -> Icons.Outlined.Bolt
}

@Composable
private fun MapLayer.toLabel(): String = when (this) {
    MapLayer.DEFAULT -> stringResource(R.string.map_layer_default)
    MapLayer.SPEED -> stringResource(R.string.map_layer_speed)
    MapLayer.POWER -> stringResource(R.string.map_layer_power)
}
