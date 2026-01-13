package com.koflox.destinations.presentation.destinations

sealed interface DestinationsUiEvent {
    data class RouteDistanceChanged(val distanceKm: Double) : DestinationsUiEvent
    data object LetsGoClicked : DestinationsUiEvent
    data object PermissionGranted : DestinationsUiEvent
    data object PermissionDenied : DestinationsUiEvent
    data object ErrorDismissed : DestinationsUiEvent
    data object ScreenResumed : DestinationsUiEvent
    data object ScreenPaused : DestinationsUiEvent
}
