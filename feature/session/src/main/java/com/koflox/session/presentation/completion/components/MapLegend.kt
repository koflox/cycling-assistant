package com.koflox.session.presentation.completion.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.component.LocalizedDropdownMenu
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.session.R

private val LEGEND_INDICATOR_SIZE = 20.dp
private const val LEGEND_DASH_LENGTH = 6f
private const val LEGEND_GAP_LENGTH = 4f

@Composable
internal fun MapLegendButton(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Light),
        shadowElevation = CornerRadius.Small,
    ) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.map_legend_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        LocalizedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            offset = DpOffset(x = 0.dp, y = Spacing.Small),
            shape = MaterialTheme.shapes.small,
        ) {
            Column(modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.Small)) {
                Text(
                    text = stringResource(R.string.map_legend_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = Spacing.Small),
                )
                LegendRow(indicator = {
                    LineIndicator(color = RouteColors.NormalSpeed)
                }, label = stringResource(R.string.map_legend_normal_speed))
                LegendRow(indicator = {
                    LineIndicator(color = RouteColors.FastSpeed)
                }, label = stringResource(R.string.map_legend_fast_speed))
                LegendRow(indicator = {
                    DashedLineIndicator(color = RouteColors.Gap)
                }, label = stringResource(R.string.map_legend_gap))
                LegendRow(indicator = {
                    ArrowIndicator(color = RouteColors.StartMarker)
                }, label = stringResource(R.string.map_legend_start))
                LegendRow(indicator = {
                    ArrowIndicator(color = RouteColors.EndMarker)
                }, label = stringResource(R.string.map_legend_finish))
            }
        }
    }
}

@Composable
private fun LegendRow(
    indicator: @Composable () -> Unit,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Spacing.Tiny),
    ) {
        indicator()
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = Spacing.Small),
        )
    }
}

@Composable
private fun LineIndicator(color: Color) {
    Canvas(modifier = Modifier.size(LEGEND_INDICATOR_SIZE)) {
        val centerY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = ROUTE_WIDTH,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DashedLineIndicator(color: Color) {
    Canvas(modifier = Modifier.size(LEGEND_INDICATOR_SIZE)) {
        val centerY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = ROUTE_WIDTH,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(LEGEND_DASH_LENGTH, LEGEND_GAP_LENGTH)),
        )
    }
}

@Composable
private fun ArrowIndicator(color: Color) {
    Canvas(modifier = Modifier.size(LEGEND_INDICATOR_SIZE)) {
        val vertices = computeArrowVertices(size.width)
        val path = Path().apply {
            moveTo(vertices.tipX, vertices.tipY)
            lineTo(vertices.baseUpperX, vertices.baseUpperY)
            lineTo(vertices.baseLowerX, vertices.baseLowerY)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}
