package com.koflox.session.presentation.completion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.navigation.STATS_SECTION_COMPLETED
import com.koflox.session.presentation.completion.components.MapHeaderOverlay
import com.koflox.session.presentation.completion.components.MapLayerSelector
import com.koflox.session.presentation.completion.components.MapLegendButton
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard

@Composable
internal fun SessionCompletionRoute(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onShareClick: () -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SessionCompletionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                SessionCompletionNavigation.ToDashboard -> onNavigateToDashboard()
            }
        }
    }
    SessionCompletionContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onShareClick = onShareClick,
        onNavigateToStatsConfig = onNavigateToStatsConfig,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
internal fun SessionCompletionContent(
    uiState: SessionCompletionUiState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onNavigateToStatsConfig: (section: String) -> Unit,
    onEvent: (SessionCompletionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SessionCompletionTopBar(
                uiState = uiState,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
            )
        },
    ) { paddingValues ->
        SessionCompletionBody(
            uiState = uiState,
            paddingValues = paddingValues,
            onNavigateToStatsConfig = onNavigateToStatsConfig,
            onEvent = onEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionCompletionTopBar(
    uiState: SessionCompletionUiState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val title = when (uiState) {
        is SessionCompletionUiState.Content -> uiState.destinationName ?: stringResource(R.string.session_free_roam_title)
        else -> stringResource(R.string.session_completion_title)
    }
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.session_completion_back))
            }
        },
        actions = {
            if (uiState is SessionCompletionUiState.Content) {
                IconButton(onClick = onShareClick) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share_content_description))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SessionCompletionBody(
    uiState: SessionCompletionUiState,
    paddingValues: PaddingValues,
    onNavigateToStatsConfig: (section: String) -> Unit,
    onEvent: (SessionCompletionUiEvent) -> Unit,
) {
    val modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    when (uiState) {
        SessionCompletionUiState.Loading -> {
            Box(modifier = modifier) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        is SessionCompletionUiState.Error -> {
            Box(modifier = modifier) {
                Text(text = uiState.message.resolve(LocalContext.current), modifier = Modifier.align(Alignment.Center))
            }
        }
        is SessionCompletionUiState.Content -> {
            SessionCompletionLayout(
                uiState = uiState,
                onNavigateToStatsConfig = onNavigateToStatsConfig,
                onEvent = onEvent,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SessionCompletionLayout(
    uiState: SessionCompletionUiState.Content,
    onNavigateToStatsConfig: (section: String) -> Unit,
    onEvent: (SessionCompletionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
        ) {
            if (uiState.routeDisplayData.allPoints.isNotEmpty()) {
                RouteMapView(
                    routeDisplayData = uiState.routeDisplayData,
                    endMarkerRotation = uiState.endMarkerRotation,
                    modifier = Modifier.matchParentSize(),
                )
            }
            MapHeaderOverlay(
                destinationName = uiState.destinationName,
                startDate = uiState.startDateFormatted,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                MapLegendButton(selectedLayer = uiState.selectedLayer)
                MapLayerSelector(
                    selectedLayer = uiState.selectedLayer,
                    availableLayers = uiState.availableLayers,
                    onLayerSelected = { onEvent(SessionCompletionUiEvent.LayerSelected(it)) },
                )
            }
        }
        SessionSummaryCard(
            stats = uiState.completedStats,
            title = stringResource(R.string.stats_config_collected_data_title),
            onEditClick = { onNavigateToStatsConfig(STATS_SECTION_COMPLETED) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
