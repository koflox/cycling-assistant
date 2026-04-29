package com.koflox.strava.impl.domain.repository

import com.koflox.strava.api.model.StravaAuthState
import kotlinx.coroutines.flow.Flow

internal interface StravaAuthRepository {
    fun observeAuthState(): Flow<StravaAuthState>
    suspend fun getCurrentAuthState(): StravaAuthState
    suspend fun logout()
}
