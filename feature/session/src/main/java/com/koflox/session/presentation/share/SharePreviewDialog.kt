package com.koflox.session.presentation.share

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SharePreviewDialogContent(
            data = data,
            isSharing = isSharing,
            graphicsLayer = graphicsLayer,
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
    graphicsLayer: GraphicsLayer,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.share_preview_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SharePreviewContent(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .drawWithCache {
                        onDrawWithContent {
                            graphicsLayer.record { this@onDrawWithContent.drawContent() }
                            drawLayer(graphicsLayer)
                        }
                    },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShareButton(isSharing = isSharing, onShareClick = onShareClick)
        }
    }
}

@Composable
private fun ShareButton(
    isSharing: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onShareClick,
        enabled = !isSharing,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.share_button))
    }
}

@Composable
private fun SharePreviewContent(
    data: SharePreviewData,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            RouteMapView(
                routePoints = data.routePoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            SessionSummaryCard(
                startDate = data.startDateFormatted,
                elapsedTime = data.elapsedTimeFormatted,
                distance = data.traveledDistanceFormatted,
                averageSpeed = data.averageSpeedFormatted,
                topSpeed = data.topSpeedFormatted,
                destinationName = data.destinationName,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
