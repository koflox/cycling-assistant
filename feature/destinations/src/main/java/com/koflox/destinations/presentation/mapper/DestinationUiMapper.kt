package com.koflox.destinations.presentation.mapper

import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.presentation.destinations.model.DestinationsUiModel
import com.koflox.location.model.Location

internal interface DestinationUiMapper {
    fun toUiModel(
        destinations: Destinations,
        userLocation: Location,
    ): DestinationsUiModel
}
