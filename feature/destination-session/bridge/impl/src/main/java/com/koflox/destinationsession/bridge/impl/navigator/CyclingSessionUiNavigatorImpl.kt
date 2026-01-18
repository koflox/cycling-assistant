package com.koflox.destinationsession.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.location.model.Location
import com.koflox.session.presentation.dialog.DestinationConfirmationDialog
import com.koflox.session.presentation.permission.NotificationPermissionHandler
import com.koflox.session.presentation.session.SessionViewModel
import com.koflox.session.presentation.sessionslist.SessionsListScreen
import org.koin.androidx.compose.koinViewModel

internal class CyclingSessionUiNavigatorImpl : CyclingSessionUiNavigator {
    @Composable
    override fun SessionScreen(
        destinationLocation: Location,
        modifier: Modifier,
    ) {
        val viewModel: SessionViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        if (state.isActive) {
            com.koflox.session.presentation.session.SessionScreen(
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
        var shouldRequestPermission by remember { mutableStateOf(false) }
        if (shouldRequestPermission) {
            NotificationPermissionHandler { isGranted ->
                if (isGranted) {
                    viewModel.startSession(
                        destinationId = destinationId,
                        destinationName = destinationName,
                        destinationLatitude = destinationLocation.latitude,
                        destinationLongitude = destinationLocation.longitude,
                        startLatitude = userLocation.latitude,
                        startLongitude = userLocation.longitude,
                    )
                    onDismiss()
                }
                @Suppress("AssignedValueIsNeverRead")
                shouldRequestPermission = false
            }
        }
        DestinationConfirmationDialog(
            destinationName = destinationName,
            distanceKm = distanceKm,
            onNavigateClick = onNavigateClick,
            onStartSessionClick = {
                @Suppress("AssignedValueIsNeverRead")
                shouldRequestPermission = true
            },
            onDismiss = onDismiss,
        )
    }

    @Composable
    override fun SessionsScreen(
        onBackClick: () -> Unit,
        modifier: Modifier,
    ) {
        SessionsListScreen(
            onBackClick = onBackClick,
            modifier = modifier,
        )
    }
}
