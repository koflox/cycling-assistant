package com.koflox.session.domain.usecase

import com.koflox.location.bearing.calculateBearingDegrees
import com.koflox.location.model.Location
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.presentation.route.RouteDisplayData
import com.koflox.session.presentation.route.buildRouteDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SessionRouteSnapshot(
    val routeDisplayData: RouteDisplayData,
    val isPaused: Boolean,
    val firstTrackPointPosition: Location?,
    val lastTrackPointPosition: Location?,
    val lastBearingDegrees: Float?,
)

interface ObserveActiveSessionRouteUseCase {
    fun observe(): Flow<SessionRouteSnapshot?>
}

internal class ObserveActiveSessionRouteUseCaseImpl(
    private val activeSessionUseCase: ActiveSessionUseCase,
) : ObserveActiveSessionRouteUseCase {

    override fun observe(): Flow<SessionRouteSnapshot?> =
        activeSessionUseCase.observeActiveSession().map { session ->
            session?.let {
                val trackPoints = it.trackPoints
                val lastTwo = trackPoints.takeLast(2)
                SessionRouteSnapshot(
                    routeDisplayData = buildRouteDisplayData(trackPoints),
                    isPaused = it.status == SessionStatus.PAUSED,
                    firstTrackPointPosition = trackPoints.firstOrNull()?.toLocation(),
                    lastTrackPointPosition = trackPoints.lastOrNull()?.toLocation(),
                    lastBearingDegrees = if (lastTwo.size == 2) {
                        calculateBearingDegrees(lastTwo[0].toLocation(), lastTwo[1].toLocation())
                    } else {
                        null
                    },
                )
            }
        }

    private fun TrackPoint.toLocation(): Location = Location(latitude = latitude, longitude = longitude)
}
