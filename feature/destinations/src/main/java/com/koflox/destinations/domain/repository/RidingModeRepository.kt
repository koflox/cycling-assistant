package com.koflox.destinations.domain.repository

import com.koflox.destinations.domain.model.RidingMode
import kotlinx.coroutines.flow.Flow

internal interface RidingModeRepository {
    fun observeRidingMode(): Flow<RidingMode>
    suspend fun setRidingMode(mode: RidingMode)
}
