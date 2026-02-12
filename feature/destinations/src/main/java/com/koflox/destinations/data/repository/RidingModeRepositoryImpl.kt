package com.koflox.destinations.data.repository

import com.koflox.destinations.data.source.local.RidingModeLocalDataSource
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidingModeRepository
import kotlinx.coroutines.flow.Flow

internal class RidingModeRepositoryImpl(
    private val localDataSource: RidingModeLocalDataSource,
) : RidingModeRepository {
    override fun observeRidingMode(): Flow<RidingMode> = localDataSource.observeRidingMode()
    override suspend fun setRidingMode(mode: RidingMode) = localDataSource.setRidingMode(mode)
}
