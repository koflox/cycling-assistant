package com.koflox.destinationsession.bridge.impl.usecase

import com.koflox.destinationsession.bridge.usecase.ActiveRouteSegment
import com.koflox.destinationsession.bridge.usecase.ActiveSessionDestination
import com.koflox.destinationsession.bridge.usecase.ActiveSessionRouteData
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.destinationsession.bridge.usecase.RouteSpan
import com.koflox.location.model.Location
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.session.domain.usecase.ObserveActiveSessionRouteUseCase
import com.koflox.session.domain.usecase.SessionRouteSnapshot
import com.koflox.session.presentation.route.ColorSpanData
import com.koflox.session.presentation.route.SegmentDisplayData
import com.koflox.session.presentation.route.buildRouteDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CyclingSessionUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val observeActiveSessionRouteUseCase: ObserveActiveSessionRouteUseCase,
) : CyclingSessionUseCase {

    override fun observeHasActiveSession(): Flow<Boolean> = activeSessionUseCase.hasActiveSession()

    override suspend fun getActiveSessionDestination(): ActiveSessionDestination? = try {
        val destinationId = activeSessionUseCase.getActiveSession().destinationId
        destinationId?.let { ActiveSessionDestination(id = it) }
    } catch (_: NoActiveSessionException) {
        null
    }

    override fun observeActiveSessionRoute(): Flow<ActiveSessionRouteData?> =
        observeActiveSessionRouteUseCase.observe().map { snapshot ->
            snapshot?.let(::mapToActiveSessionRouteData)
        }

    private fun mapToActiveSessionRouteData(snapshot: SessionRouteSnapshot): ActiveSessionRouteData {
        val routeData = buildRouteDisplayData(snapshot.trackPoints)
        val segments = routeData.segments.map { it.toActiveRouteSegment() }
        val gapPolylines = routeData.gapPolylines.map { points ->
            val first = points.first()
            val last = points.last()
            Location(first.latitude, first.longitude) to Location(last.latitude, last.longitude)
        }
        val lastSpanColor = routeData.segments.lastOrNull()?.colorSpans?.lastOrNull()?.endColorArgb
        return ActiveSessionRouteData(
            segments = segments,
            gapPolylines = gapPolylines,
            startPosition = snapshot.firstTrackPointPosition,
            lastPosition = snapshot.lastTrackPointPosition,
            lastSpanColorArgb = lastSpanColor,
            lastBearingDegrees = snapshot.lastBearingDegrees,
            isPaused = snapshot.isPaused,
            showGapToUserLocation = snapshot.showGapToUserLocation,
        )
    }

    private fun SegmentDisplayData.toActiveRouteSegment(): ActiveRouteSegment = ActiveRouteSegment(
        points = points.map { Location(it.latitude, it.longitude) },
        spans = colorSpans.map { it.toRouteSpan() },
    )

    private fun ColorSpanData.toRouteSpan(): RouteSpan = when (this) {
        is ColorSpanData.Solid -> RouteSpan.Solid(colorArgb = colorArgb, length = length)
        is ColorSpanData.Gradient -> RouteSpan.Gradient(
            fromColorArgb = fromColorArgb,
            toColorArgb = toColorArgb,
            length = length,
        )
    }
}
