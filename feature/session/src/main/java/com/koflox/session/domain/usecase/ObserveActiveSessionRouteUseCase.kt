package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.presentation.route.RouteDisplayData
import com.koflox.session.presentation.route.buildRouteDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SessionRouteSnapshot(
    val routeDisplayData: RouteDisplayData,
    val isPaused: Boolean,
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
                SessionRouteSnapshot(
                    routeDisplayData = buildRouteDisplayData(it.trackPoints),
                    isPaused = it.status == SessionStatus.PAUSED,
                )
            }
        }
}
