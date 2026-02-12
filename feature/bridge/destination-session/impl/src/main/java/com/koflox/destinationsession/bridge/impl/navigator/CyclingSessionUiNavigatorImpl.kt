package com.koflox.destinationsession.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.destinationsession.bridge.navigator.CyclingSessionUiNavigator
import com.koflox.location.model.Location
import com.koflox.session.presentation.dialog.DestinationOptionsRoute
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
    override fun DestinationOptions(
        destinationId: String,
        destinationName: String,
        destinationLocation: Location,
        distanceKm: Double,
        onNavigateClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        DestinationOptionsRoute(
            destinationId = destinationId,
            destinationName = destinationName,
            destinationLocation = destinationLocation,
            distanceKm = distanceKm,
            onNavigateClick = onNavigateClick,
            onDismiss = onDismiss,
        )
    }
}
