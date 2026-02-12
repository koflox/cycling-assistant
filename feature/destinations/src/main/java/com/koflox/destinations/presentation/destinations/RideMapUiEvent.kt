package com.koflox.destinations.presentation.destinations

import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel

internal sealed interface RideMapUiEvent {

    sealed interface ModeEvent : RideMapUiEvent {
        data class ModeSelected(val mode: RidingMode) : ModeEvent
    }

    sealed interface LifecycleEvent : RideMapUiEvent {
        data object ScreenResumed : LifecycleEvent
        data object ScreenPaused : LifecycleEvent
        data object RetryInitializationClicked : LifecycleEvent
    }

    sealed interface PermissionEvent : RideMapUiEvent {
        data object PermissionGranted : PermissionEvent
        data object PermissionDenied : PermissionEvent
    }

    sealed interface DestinationEvent : RideMapUiEvent {
        data class RouteDistanceChanged(val distanceKm: Double) : DestinationEvent
        data object LetsGoClicked : DestinationEvent
        data class OpenInGoogleMaps(val destination: DestinationUiModel) : DestinationEvent
        data object SelectedMarkerInfoClicked : DestinationEvent
        data object SelectedMarkerOptionsDialogDismissed : DestinationEvent
    }

    sealed interface SessionEvent : RideMapUiEvent {
        data object StartFreeRoamClicked : SessionEvent
    }

    sealed interface CommonEvent : RideMapUiEvent {
        data object ErrorDismissed : CommonEvent
        data object NavigationActionHandled : CommonEvent
        data object NutritionPopupDismissed : CommonEvent
    }
}
