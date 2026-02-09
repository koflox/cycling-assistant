package com.koflox.session.presentation.share

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.koflox.designsystem.theme.Spacing
import com.koflox.designsystem.theme.SurfaceAlpha
import com.koflox.session.R
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SharePreviewDialog(
    data: SharePreviewData,
    isSharing: Boolean,
    onShareClick: (bitmap: Bitmap, destinationName: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }

    Dialog(
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
                    onShareClick(bitmap, data.destinationName)
                }
            },
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
            ShareButton(
                isSharing = isSharing,
                isEnabled = isMapLoaded && !isSharing,
                onShareClick = onShareClick,
            )
        }
    }
}

@Composable
private fun ShareButton(
    isSharing: Boolean,
    isEnabled: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onShareClick,
        enabled = isEnabled,
        modifier = modifier.fillMaxWidth(),
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
                    routePoints = data.routePoints,
                    startMarkerRotation = data.startMarkerRotation,
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
                startDate = data.startDateFormatted,
                elapsedTime = data.elapsedTimeFormatted,
                movingTime = data.movingTimeFormatted,
                idleTime = data.idleTimeFormatted,
                distance = data.traveledDistanceFormatted,
                averageSpeed = data.averageSpeedFormatted,
                topSpeed = data.topSpeedFormatted,
                altitudeGain = data.altitudeGainFormatted,
                altitudeLoss = data.altitudeLossFormatted,
                calories = data.caloriesFormatted,
                isCompact = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MapHeaderOverlay(
    destinationName: String,
    startDate: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = SurfaceAlpha.Transparant))
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destinationName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = startDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
