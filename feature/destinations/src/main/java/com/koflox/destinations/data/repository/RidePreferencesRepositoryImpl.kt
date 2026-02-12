package com.koflox.destinations.data.repository

import com.koflox.destinations.data.source.local.RidingModeLocalDataSource
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import kotlinx.coroutines.flow.Flow

internal class RidePreferencesRepositoryImpl(
    private val localDataSource: RidingModeLocalDataSource,
) : RidePreferencesRepository {
    override fun observeRidingMode(): Flow<RidingMode> = localDataSource.observeRidingMode()
    override suspend fun setRidingMode(mode: RidingMode) = localDataSource.setRidingMode(mode)
}
