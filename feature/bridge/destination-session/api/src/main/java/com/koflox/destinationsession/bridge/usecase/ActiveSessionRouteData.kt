package com.koflox.destinationsession.bridge.usecase

import com.koflox.location.model.Location

data class ActiveSessionRouteData(
    val segments: List<ActiveRouteSegment>,
    val gapPolylines: List<Pair<Location, Location>>,
    val startPosition: Location?,
    val lastPosition: Location?,
    val lastSpanColorArgb: Int?,
    val lastBearingDegrees: Float?,
    val isPaused: Boolean,
)

data class ActiveRouteSegment(
    val points: List<Location>,
    val spans: List<RouteSpan>,
)

sealed interface RouteSpan {
    val length: Double

    data class Solid(
        val colorArgb: Int,
        override val length: Double,
    ) : RouteSpan

    data class Gradient(
        val fromColorArgb: Int,
        val toColorArgb: Int,
        override val length: Double,
    ) : RouteSpan
}
