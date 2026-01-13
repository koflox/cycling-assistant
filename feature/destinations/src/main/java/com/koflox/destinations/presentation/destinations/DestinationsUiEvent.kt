package com.koflox.destinations.presentation.destinations

import com.koflox.destinations.presentation.destinations.model.DestinationUiModel

internal sealed interface DestinationsUiEvent {
    data class RouteDistanceChanged(val distanceKm: Double) : DestinationsUiEvent
    data object LetsGoClicked : DestinationsUiEvent
    data object PermissionGranted : DestinationsUiEvent
    data object PermissionDenied : DestinationsUiEvent
    data object ErrorDismissed : DestinationsUiEvent
    data object ScreenResumed : DestinationsUiEvent
    data object ScreenPaused : DestinationsUiEvent
    data class OpenDestinationInGoogleMaps(val destination: DestinationUiModel) : DestinationsUiEvent
    data object NavigationActionHandled : DestinationsUiEvent
}
