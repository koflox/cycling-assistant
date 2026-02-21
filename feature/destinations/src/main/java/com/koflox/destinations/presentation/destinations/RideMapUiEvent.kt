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
        data class PermissionDenied(val isRationaleAvailable: Boolean) : PermissionEvent
    }

    sealed interface DestinationEvent : RideMapUiEvent {
        data class RouteDistanceChanged(val distanceKm: Double) : DestinationEvent
        data object LetsGoClicked : DestinationEvent
        data class OpenInGoogleMaps(val destination: DestinationUiModel) : DestinationEvent
        data object SelectedMarkerInfoClicked : DestinationEvent
        data object SelectedMarkerOptionsDialogDismissed : DestinationEvent
    }

    sealed interface PoiEvent : RideMapUiEvent {
        data class CoffeeShopClicked(val query: String) : PoiEvent
        data class ToiletClicked(val query: String) : PoiEvent
    }

    sealed interface SessionEvent : RideMapUiEvent {
        data object FreeRoamSessionStarting : SessionEvent
        data object DestinationSessionStarting : SessionEvent
    }

    sealed interface CommonEvent : RideMapUiEvent {
        data object ErrorDismissed : CommonEvent
        data object NavigationActionHandled : CommonEvent
        data object NutritionPopupDismissed : CommonEvent
    }

    sealed interface MapEvent : RideMapUiEvent {
        data object MapLoaded : MapEvent
    }
}
