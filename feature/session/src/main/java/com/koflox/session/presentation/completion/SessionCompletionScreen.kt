package com.koflox.session.presentation.completion

import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import com.koflox.session.presentation.share.SharePreviewData
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

    LaunchedEffect(uiState.shouldNavigateToDashboard) {
        if (uiState.shouldNavigateToDashboard) {
            onNavigateToDashboard()
        }
    }
    LaunchedEffect(uiState.shareIntent) {
        uiState.shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onEvent(SessionCompletionUiEvent.ShareIntentLaunched)
        }
    }
    LaunchedEffect(uiState.error, uiState.showShareDialog) {
        if (uiState.showShareDialog && uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SessionCompletionUiEvent.ErrorDismissed)
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
private fun SessionCompletionContent(
    uiState: SessionCompletionUiState,
    onBackClick: () -> Unit,
    onEvent: (SessionCompletionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.showShareDialog && uiState.sessionId.isNotEmpty()) {
        SharePreviewDialog(
            data = SharePreviewData(
                sessionId = uiState.sessionId,
                destinationName = uiState.destinationName,
                startDateFormatted = uiState.startDateFormatted,
                elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                traveledDistanceFormatted = uiState.traveledDistanceFormatted,
                averageSpeedFormatted = uiState.averageSpeedFormatted,
                topSpeedFormatted = uiState.topSpeedFormatted,
                routePoints = uiState.routePoints,
            ),
            isSharing = uiState.isSharing,
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
    TopAppBar(
        title = { Text(text = uiState.destinationName.ifEmpty { stringResource(R.string.session_completion_title) }) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.session_completion_back))
            }
        },
        actions = {
            if (!uiState.isLoading && uiState.error == null) {
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
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error.orEmpty(),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                val cardAlignment = calculateCardAlignment(uiState.routePoints)
                RouteMapView(routePoints = uiState.routePoints, modifier = Modifier.fillMaxSize())
                SessionSummaryCard(
                    startDate = uiState.startDateFormatted,
                    elapsedTime = uiState.elapsedTimeFormatted,
                    distance = uiState.traveledDistanceFormatted,
                    averageSpeed = uiState.averageSpeedFormatted,
                    topSpeed = uiState.topSpeedFormatted,
                    modifier = Modifier.align(cardAlignment),
                )
            }
        }
    }
}
