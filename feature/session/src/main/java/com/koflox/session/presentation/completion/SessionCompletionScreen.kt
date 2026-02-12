package com.koflox.session.presentation.completion

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.koflox.session.R
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard
import com.koflox.session.presentation.completion.components.calculateCardAlignment
import com.koflox.session.presentation.share.SharePreviewDialog
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SessionCompletionRoute(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionCompletionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                SessionCompletionNavigation.ToDashboard -> onNavigateToDashboard()
            }
        }
    }
    LaunchedEffect(uiState) {
        val content = uiState as? SessionCompletionUiState.Content ?: return@LaunchedEffect
        when (val overlay = content.overlay) {
            is Overlay.ShareReady -> {
                context.startActivity(overlay.intent)
                viewModel.onEvent(SessionCompletionUiEvent.ShareIntentLaunched)
            }

            is Overlay.ShareError -> {
                Toast.makeText(context, overlay.message, Toast.LENGTH_SHORT).show()
                viewModel.onEvent(SessionCompletionUiEvent.ErrorDismissed)
            }

            else -> Unit
        }
    }
    SessionCompletionContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
internal fun SessionCompletionContent(
    uiState: SessionCompletionUiState,
    onBackClick: () -> Unit,
    onEvent: (SessionCompletionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? SessionCompletionUiState.Content
    val sharePreviewData = when (val overlay = content?.overlay) {
        is Overlay.ShareDialog -> overlay.sharePreviewData
        is Overlay.Sharing -> overlay.sharePreviewData
        else -> null
    }
    if (sharePreviewData != null) {
        SharePreviewDialog(
            data = sharePreviewData,
            isSharing = content?.overlay is Overlay.Sharing,
            onShareClick = { bitmap, destinationName ->
                onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, destinationName))
            },
            onDismiss = { onEvent(SessionCompletionUiEvent.ShareDialogDismissed) },
        )
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            SessionCompletionTopBar(
                uiState = uiState,
                onBackClick = onBackClick,
                onShareClick = { onEvent(SessionCompletionUiEvent.ShareClicked) },
            )
        },
    ) { paddingValues ->
        SessionCompletionBody(uiState = uiState, paddingValues = paddingValues)
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
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        when (uiState) {
            SessionCompletionUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is SessionCompletionUiState.Error -> {
                Text(
                    text = uiState.message,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is SessionCompletionUiState.Content -> {
                val cardAlignment = calculateCardAlignment(uiState.routePoints)
                if (uiState.routePoints.isNotEmpty()) {
                    RouteMapView(
                        routePoints = uiState.routePoints,
                        startMarkerRotation = uiState.startMarkerRotation,
                        endMarkerRotation = uiState.endMarkerRotation,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                SessionSummaryCard(
                    startDate = uiState.startDateFormatted,
                    elapsedTime = uiState.elapsedTimeFormatted,
                    movingTime = uiState.movingTimeFormatted,
                    idleTime = uiState.idleTimeFormatted,
                    distance = uiState.traveledDistanceFormatted,
                    averageSpeed = uiState.averageSpeedFormatted,
                    topSpeed = uiState.topSpeedFormatted,
                    altitudeGain = uiState.altitudeGainFormatted,
                    altitudeLoss = uiState.altitudeLossFormatted,
                    calories = uiState.caloriesFormatted,
                    modifier = Modifier.align(cardAlignment),
                )
            }
        }
    }
}
