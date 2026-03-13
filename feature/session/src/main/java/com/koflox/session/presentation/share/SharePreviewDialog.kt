package com.koflox.session.presentation.share

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.LocalizedDialog
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R

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
        PrimaryTabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            Tab(
                selected = uiState.selectedTab == ShareTab.IMAGE,
                onClick = { onEvent(ShareUiEvent.TabSelected(ShareTab.IMAGE)) },
                text = { Text(stringResource(R.string.share_tab_image)) },
            )
            Tab(
                selected = uiState.selectedTab == ShareTab.GPX,
                onClick = { onEvent(ShareUiEvent.TabSelected(ShareTab.GPX)) },
                text = { Text(stringResource(R.string.share_tab_gpx)) },
            )
        }
        Spacer(modifier = Modifier.height(Spacing.Medium))
        when (uiState.selectedTab) {
            ShareTab.IMAGE -> ImageShareTab(
                data = uiState.sharePreviewData,
                imageShareState = uiState.imageShareState,
                onEvent = onEvent,
                onNavigateToStatsConfig = onNavigateToStatsConfig,
            )

            ShareTab.GPX -> GpxShareTab(
                gpxShareState = uiState.gpxShareState,
                onEvent = onEvent,
            )
        }
    }
}
