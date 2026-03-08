package com.koflox.session.testutil

import com.koflox.session.presentation.mapper.SessionUiModel
import com.koflox.session.presentation.sessionslist.SessionListItemStatus
import com.koflox.session.presentation.sessionslist.SessionListItemUiModel

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

internal fun createSessionListItemUiModel(
    id: String = "",
    destinationName: String = "",
    dateFormatted: String = "",
    distanceFormatted: String = "",
    status: SessionListItemStatus = SessionListItemStatus.COMPLETED,
    isShareButtonVisible: Boolean = false,
) = SessionListItemUiModel(
    id = id,
    destinationName = destinationName,
    dateFormatted = dateFormatted,
    distanceFormatted = distanceFormatted,
    status = status,
    isShareButtonVisible = isShareButtonVisible,
)
