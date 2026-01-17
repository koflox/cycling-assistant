package com.koflox.destinationsession.bridge.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.koflox.destinationsession.bridge.DestinationSessionBridge
import com.koflox.location.model.Location
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.presentation.dialog.DestinationConfirmationDialog
import com.koflox.session.presentation.session.SessionScreen
import com.koflox.session.presentation.session.SessionViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel

internal class DestinationSessionBridgeImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
) : DestinationSessionBridge {

    override fun observeHasActiveSession(): Flow<Boolean> = activeSessionUseCase.hasActiveSession()

    @Composable
    override fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
    ) {
        val viewModel: SessionViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        if (state.isActive) {
            SessionScreen(
                viewModel = viewModel,
                modifier = modifier,
            )
        }
    }

    @Composable
    override fun DestinationOptions(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        userLocation: Location,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val viewModel: SessionViewModel = koinViewModel()

        DestinationConfirmationDialog(
            destinationName = destinationName,
            distanceKm = distanceKm,
            onNavigateClick = onNavigateClick,
            onStartSessionClick = {
                viewModel.startSession(
                    destinationId = destinationId,
                    destinationName = destinationName,
                    destinationLatitude = destinationLocation.latitude,
                    destinationLongitude = destinationLocation.longitude,
                    startLatitude = userLocation.latitude,
                    startLongitude = userLocation.longitude,
                )
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}
