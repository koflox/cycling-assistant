package com.koflox.poi.data.repository

import com.koflox.poi.data.source.PoiLocalDataSource
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.repository.PoiRepository
import kotlinx.coroutines.flow.Flow

internal class PoiRepositoryImpl(
    private val localDataSource: PoiLocalDataSource,
) : PoiRepository {

    override fun observeSelectedPois(): Flow<List<PoiType>> = localDataSource.observeSelectedPois()

    override suspend fun updateSelectedPois(pois: List<PoiType>) {
        localDataSource.updateSelectedPois(pois)
    }
}
