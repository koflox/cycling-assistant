package com.koflox.cyclingassistant.presentation.mapper

import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.domain.usecase.Destinations
import com.koflox.cyclingassistant.presentation.destinations.model.DestinationsUiModel

internal interface DestinationUiMapper {
    fun toUiModel(
        destinations: Destinations,
        userLocation: Location,
    ): DestinationsUiModel

}
