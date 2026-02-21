package com.koflox.destinationsession.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.location.model.Location
import com.koflox.session.presentation.dialog.DestinationOptionsRoute
import com.koflox.session.presentation.dialog.FreeRoamSessionRoute
import com.koflox.session.presentation.session.SessionScreenRoute

internal class CyclingSessionUiNavigatorImpl : CyclingSessionUiNavigator {

    @Composable
    override fun SessionScreen(
        destinationLocation: Location?,
        modifier: Modifier,
        onNavigateToCompletion: (sessionId: String) -> Unit,
    ) {
        SessionScreenRoute(
            onNavigateToCompletion = onNavigateToCompletion,
            modifier = modifier,
        )
    }

    @Composable
    override fun StartFreeRoamSession() {
        FreeRoamSessionRoute()
    }

    @Composable
    override fun DestinationOptions(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        isNavigateVisible: Boolean,
        onSessionStarting: () -> Unit,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        DestinationOptionsRoute(
            destinationId = destinationId,
            destinationName = destinationName,
            destinationLocation = destinationLocation,
            distanceKm = distanceKm,
            isNavigateVisible = isNavigateVisible,
            onSessionStarting = onSessionStarting,
            onNavigateClick = onNavigateClick,
            onDismiss = onDismiss,
        )
    }
}
