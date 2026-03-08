package com.koflox.session.presentation.share

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.LocalizedDialog
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.presentation.completion.components.MapHeaderOverlay
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SharePreviewDialog(
    data: SharePreviewData,
    isSharing: Boolean,
    onShareClick: (bitmap: Bitmap, shareText: String, chooserTitle: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onEditStatsClick: (() -> Unit)? = null,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }
    val shareText = if (data.destinationName != null) {
        stringResource(R.string.share_text, data.destinationName)
    } else {
        stringResource(R.string.share_text_free_roam)
    }
    val chooserTitle = stringResource(R.string.share_chooser_title)
    LocalizedDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SharePreviewDialogContent(
            data = data,
            isSharing = isSharing,
            isMapLoaded = isMapLoaded,
            graphicsLayer = graphicsLayer,
            onMapLoaded = {
                @Suppress("AssignedValueIsNeverRead")
                isMapLoaded = true
            },
            onShareClick = {
                scope.launch {
                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    onShareClick(bitmap, shareText, chooserTitle)
                }
            },
            onEditStatsClick = onEditStatsClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun SharePreviewDialogContent(
    data: SharePreviewData,
    isSharing: Boolean,
    isMapLoaded: Boolean,
    graphicsLayer: GraphicsLayer,
    onMapLoaded: () -> Unit,
    onShareClick: () -> Unit,
    onEditStatsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.share_preview_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(Spacing.Large))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f),
                contentAlignment = Alignment.Center,
            ) {
                SharePreviewContent(
                    data = data,
                    onMapLoaded = onMapLoaded,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithCache {
                            val recordSize = IntSize(size.width.toInt(), size.height.toInt())
                            onDrawWithContent {
                                graphicsLayer.record(size = recordSize) {
                                    this@onDrawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                        },
                )
                if (!isMapLoaded) {
                    CircularProgressIndicator()
                }
            }
            Spacer(modifier = Modifier.height(Spacing.Large))
            ShareActionRow(
                isSharing = isSharing,
                isEnabled = isMapLoaded && !isSharing,
                onShareClick = onShareClick,
                onEditStatsClick = onEditStatsClick,
            )
        }
    }
}

@Composable
private fun ShareActionRow(
    isSharing: Boolean,
    isEnabled: Boolean,
    onShareClick: () -> Unit,
    onEditStatsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DebouncedButton(
            onClick = onShareClick,
            enabled = isEnabled,
            modifier = Modifier.weight(1f),
        ) {
            if (isSharing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(Spacing.Large),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Spacing.Tiny / 2,
                )
            } else {
                Text(stringResource(R.string.share_button))
            }
        }
        if (onEditStatsClick != null) {
            IconButton(
                onClick = onEditStatsClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.stats_config_edit_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SharePreviewContent(
    data: SharePreviewData,
    onMapLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
            ) {
                RouteMapView(
                    routeDisplayData = data.routeDisplayData,
                    endMarkerRotation = data.endMarkerRotation,
                    isSharePreview = true,
                    onMapLoaded = onMapLoaded,
                    modifier = Modifier.matchParentSize(),
                )
                MapHeaderOverlay(
                    destinationName = data.destinationName,
                    startDate = data.startDateFormatted,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            SessionSummaryCard(
                stats = data.shareStats,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
