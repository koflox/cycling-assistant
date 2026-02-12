package com.koflox.destinations.data.source.local

import com.koflox.destinations.domain.model.RidingMode
import kotlinx.coroutines.flow.Flow

internal interface RidingModeLocalDataSource {
    fun observeRidingMode(): Flow<RidingMode>
    suspend fun setRidingMode(mode: RidingMode)
}
