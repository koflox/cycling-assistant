package com.koflox.cyclingassistant.presentation.destinations.model

import com.koflox.cyclingassistant.domain.model.Location

internal data class DestinationUiModel(
    val id: String,
    val title: String,
    val location: Location,
    val distanceKm: Double,
    val isMain: Boolean,
)

internal data class DestinationsUiModel(
    val selected: DestinationUiModel,
    val otherValidDestinations: List<DestinationUiModel>,
)
