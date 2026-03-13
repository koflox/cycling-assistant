package com.koflox.session.presentation.share

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.LocalizedDialog
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.presentation.completion.components.MapHeaderOverlay
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard
import kotlinx.coroutines.launch

@Composable
internal fun ShareSessionRoute(
    onDismiss: () -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ShareViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        val content = uiState as? ShareUiState.Content ?: return@LaunchedEffect
        when (val imageState = content.imageShareState) {
            is ImageShareState.Ready -> {
                context.startActivity(imageState.intent)
                viewModel.onEvent(ShareUiEvent.Image.IntentLaunched)
            }

            is ImageShareState.Error -> {
                Toast.makeText(context, imageState.message.resolve(context), Toast.LENGTH_SHORT).show()
                viewModel.onEvent(ShareUiEvent.Image.ErrorDismissed)
            }

            else -> Unit
        }
        when (val gpxState = content.gpxShareState) {
            is GpxShareState.Ready -> {
                context.startActivity(gpxState.intent)
                viewModel.onEvent(ShareUiEvent.Gpx.IntentLaunched)
            }

            is GpxShareState.Error -> {
                Toast.makeText(context, gpxState.message.resolve(context), Toast.LENGTH_SHORT).show()
                viewModel.onEvent(ShareUiEvent.Gpx.ErrorDismissed)
            }

            else -> Unit
        }
    }
    ShareSessionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onDismiss = onDismiss,
        onNavigateToStatsConfig = onNavigateToStatsConfig,
        modifier = modifier,
    )
}

@Composable
internal fun ShareSessionContent(
    uiState: ShareUiState,
    onEvent: (ShareUiEvent) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalizedDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(Spacing.Large),
            shape = MaterialTheme.shapes.large,
        ) {
            when (uiState) {
                ShareUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Huge),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ShareUiState.Content -> {
                    ShareSessionContentBody(
                        uiState = uiState,
                        onEvent = onEvent,
                        onNavigateToStatsConfig = onNavigateToStatsConfig,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSessionContentBody(
    uiState: ShareUiState.Content,
    onEvent: (ShareUiEvent) -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.share_preview_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        PrimaryTabRow(selectedTabIndex = uiState.selectedTabIndex) {
            Tab(
                selected = uiState.selectedTabIndex == 0,
                onClick = { onEvent(ShareUiEvent.TabSelected(0)) },
                text = { Text(stringResource(R.string.share_tab_image)) },
            )
            Tab(
                selected = uiState.selectedTabIndex == 1,
                onClick = { onEvent(ShareUiEvent.TabSelected(1)) },
                text = { Text(stringResource(R.string.share_tab_gpx)) },
            )
        }
        Spacer(modifier = Modifier.height(Spacing.Medium))
        when (uiState.selectedTabIndex) {
            0 -> ImageShareTab(
                data = uiState.sharePreviewData,
                imageShareState = uiState.imageShareState,
                onEvent = onEvent,
                onNavigateToStatsConfig = onNavigateToStatsConfig,
            )

            1 -> GpxShareTab(
                gpxShareState = uiState.gpxShareState,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImageShareTab(
    data: SharePreviewData,
    imageShareState: ImageShareState,
    onEvent: (ShareUiEvent) -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
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
    val isSharing = imageShareState is ImageShareState.Sharing
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            contentAlignment = Alignment.Center,
        ) {
            SharePreviewContent(
                data = data,
                onMapLoaded = {
                    @Suppress("AssignedValueIsNeverRead")
                    isMapLoaded = true
                },
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
            onShareClick = {
                scope.launch {
                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    onEvent(ShareUiEvent.Image.ShareConfirmed(bitmap, shareText, chooserTitle))
                }
            },
            onEditStatsClick = { onNavigateToStatsConfig(com.koflox.session.navigation.STATS_SECTION_SHARE) },
        )
    }
}

@Composable
internal fun GpxShareTab(
    gpxShareState: GpxShareState,
    onEvent: (ShareUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        when (gpxShareState) {
            GpxShareState.Idle -> GpxIdleContent(onExportClick = { onEvent(ShareUiEvent.Gpx.ShareClicked) })
            GpxShareState.Generating -> GpxGeneratingContent()
            is GpxShareState.Ready,
            is GpxShareState.Error,
                -> GpxExportableContent(onExportClick = { onEvent(ShareUiEvent.Gpx.ShareClicked) })

            GpxShareState.Unavailable -> GpxUnavailableContent()
        }
    }
}

@Composable
private fun GpxIdleContent(onExportClick: () -> Unit) {
    GpxFileIcon(tint = MaterialTheme.colorScheme.primary)
    Text(
        text = stringResource(R.string.gpx_export_description),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = onExportClick, isEnabled = true)
}

@Composable
private fun GpxGeneratingContent() {
    CircularProgressIndicator()
    Text(
        text = stringResource(R.string.gpx_export_generating),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = {}, isEnabled = false)
}

@Composable
private fun GpxExportableContent(onExportClick: () -> Unit) {
    GpxFileIcon(tint = MaterialTheme.colorScheme.primary)
    GpxExportButton(onClick = onExportClick, isEnabled = true)
}

@Composable
private fun GpxUnavailableContent() {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Text(
        text = stringResource(R.string.gpx_export_no_app),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = {}, isEnabled = false)
}

@Composable
private fun GpxFileIcon(tint: Color) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = tint,
    )
}

@Composable
private fun GpxExportButton(onClick: () -> Unit, isEnabled: Boolean) {
    DebouncedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.gpx_export_button))
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
