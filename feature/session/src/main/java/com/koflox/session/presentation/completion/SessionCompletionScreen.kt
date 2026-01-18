package com.koflox.session.presentation.completion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import com.koflox.session.R
import com.koflox.session.presentation.completion.components.RouteMapView
import com.koflox.session.presentation.completion.components.SessionSummaryCard
import com.koflox.session.presentation.completion.components.calculateCardAlignment
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionCompletionRoute(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionCompletionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldNavigateToDashboard) {
        if (uiState.shouldNavigateToDashboard) {
            onNavigateToDashboard()
        }
    }

    SessionCompletionContent(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionCompletionContent(
    uiState: SessionCompletionUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.destinationName.ifEmpty {
                            stringResource(R.string.session_completion_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.session_completion_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        SessionCompletionBody(uiState = uiState, paddingValues = paddingValues)
    }
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
