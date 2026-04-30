package com.koflox.strava.impl.data.repository

import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.data.source.local.StravaTokenLocalDataSource
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import com.koflox.strava.impl.domain.repository.StravaAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class StravaAuthRepositoryImpl @Inject constructor(
    private val localDataSource: StravaTokenLocalDataSource,
) : StravaAuthRepository {

    override fun observeAuthState(): Flow<StravaAuthState> = localDataSource
        .observe()
        .map { it.toAuthState() }

    override suspend fun getCurrentAuthState(): StravaAuthState =
        localDataSource.get().toAuthState()

    override suspend fun logout() {
        localDataSource.delete()
    }

    private fun StravaTokenEntity?.toAuthState(): StravaAuthState = if (this == null) {
        StravaAuthState.LoggedOut
    } else {
        StravaAuthState.LoggedIn(athleteId = athleteId, athleteName = athleteName)
    }
}
