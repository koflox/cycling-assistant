package com.koflox.session.testutil

import com.koflox.session.presentation.mapper.SessionUiModel

internal fun createSessionUiModel(
    elapsedTimeFormatted: String = "",
    traveledDistanceFormatted: String = "",
    averageSpeedFormatted: String = "",
    topSpeedFormatted: String = "",
    altitudeGainFormatted: String = "",
) = SessionUiModel(
    elapsedTimeFormatted = elapsedTimeFormatted,
    traveledDistanceFormatted = traveledDistanceFormatted,
    averageSpeedFormatted = averageSpeedFormatted,
    topSpeedFormatted = topSpeedFormatted,
    altitudeGainFormatted = altitudeGainFormatted,
)
