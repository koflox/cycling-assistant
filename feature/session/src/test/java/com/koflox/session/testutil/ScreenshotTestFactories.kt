package com.koflox.session.testutil

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.model.DisplayStat
import com.koflox.session.presentation.session.DeviceStripItem
import com.koflox.session.presentation.session.DeviceStripState
import com.koflox.session.presentation.session.SessionOverlay
import com.koflox.session.presentation.session.SessionUiState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun createActiveSessionState(
    sessionId: String = "session-1",
    destinationName: String? = null,
    status: SessionStatus = SessionStatus.RUNNING,
    elapsedTimeFormatted: String = "01:23:45",
    traveledDistanceFormatted: String = "12.5 km",
    averageSpeedFormatted: String = "25.3 km/h",
    topSpeedFormatted: String = "42.1 km/h",
    altitudeGainFormatted: String = "320 m",
    stats: List<DisplayStat> = createDefaultStats(
        elapsedTimeFormatted = elapsedTimeFormatted,
        traveledDistanceFormatted = traveledDistanceFormatted,
        averageSpeedFormatted = averageSpeedFormatted,
    ),
    isLocationDisabled: Boolean = false,
    overlay: SessionOverlay? = null,
    deviceStripItems: List<DeviceStripItem> = emptyList(),
) = SessionUiState.Active(
    sessionId = sessionId,
    destinationName = destinationName,
    destinationLocation = null,
    status = status,
    elapsedTimeFormatted = elapsedTimeFormatted,
    traveledDistanceFormatted = traveledDistanceFormatted,
    averageSpeedFormatted = averageSpeedFormatted,
    topSpeedFormatted = topSpeedFormatted,
    altitudeGainFormatted = altitudeGainFormatted,
    stats = stats,
    currentLocation = null,
    isLocationDisabled = isLocationDisabled,
    overlay = overlay,
    deviceStripItems = deviceStripItems,
)

internal fun createDefaultStats(
    elapsedTimeFormatted: String = "01:23:45",
    traveledDistanceFormatted: String = "12.5 km",
    averageSpeedFormatted: String = "25.3 km/h",
) = listOf(
    DisplayStat(label = "Time", value = elapsedTimeFormatted),
    DisplayStat(label = "Distance", value = traveledDistanceFormatted),
    DisplayStat(label = "Avg Speed", value = averageSpeedFormatted),
)

internal fun createConnectedDevice(
    deviceName: String = "Favero Assioma",
    powerWatts: Int = 245,
) = DeviceStripItem(
    deviceName = deviceName,
    state = DeviceStripState.Connected(instantaneousPowerWatts = powerWatts),
)

internal fun createReconnectingDevice(
    deviceName: String = "Favero Assioma",
    remaining: Duration = 12.seconds,
) = DeviceStripItem(
    deviceName = deviceName,
    state = DeviceStripState.Reconnecting(remaining = remaining),
)
