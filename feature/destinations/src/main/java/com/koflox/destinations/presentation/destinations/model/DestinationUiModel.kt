package com.koflox.destinations.presentation.destinations.model

import com.koflox.designsystem.text.UiText
import com.koflox.location.model.Location

internal data class DestinationUiModel(
    val id: String,
    val title: String,
    val location: Location,
    val distanceKm: Double,
    val distanceFormatted: UiText,
    val isMain: Boolean,
)

internal data class DestinationsUiModel(
    val selected: DestinationUiModel,
    val otherValidDestinations: List<DestinationUiModel>,
)
