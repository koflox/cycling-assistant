package com.koflox.destinations.domain.model

internal sealed interface DestinationLoadingEvent {
    data object Loading : DestinationLoadingEvent
    data object Completed : DestinationLoadingEvent
    data class Error(val throwable: Throwable) : DestinationLoadingEvent
}
