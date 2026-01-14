package com.koflox.destinationsession.bridge.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.koflox.destinationsession.bridge.DestinationSessionBridge
import com.koflox.location.model.Location
import com.koflox.session.presentation.dialog.DestinationConfirmationDialog
import com.koflox.session.presentation.session.SessionScreen
import com.koflox.session.presentation.session.SessionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.compose.koinViewModel

// TODO: _hasActiveSession flow should be a part of repository, exposed through a UC,
//  all modifications should first go to VM, then UC and then repository
internal class DestinationSessionBridgeImpl : DestinationSessionBridge {

    private val _hasActiveSession = MutableStateFlow(false)
    override val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    // TODO: fix unused onSessionEnded, decide whether keep it or not
    @Composable
    override fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
    ) {
        val viewModel: SessionViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(state.isActive) {
            _hasActiveSession.value = state.isActive
        }

        if (state.isActive) {
            SessionScreen(
                viewModel = viewModel,
                modifier = modifier,
            )
        }
    }

    @Composable
    override fun ConfirmationDialog(
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
                    onSessionEnded = {
                        _hasActiveSession.value = false
                    },
                )
                _hasActiveSession.value = true
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}
