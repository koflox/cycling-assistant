package com.koflox.poi.data.source

import com.koflox.poi.domain.model.PoiType
import kotlinx.coroutines.flow.Flow

internal interface PoiLocalDataSource {
    fun observeSelectedPois(): Flow<List<PoiType>>
    suspend fun updateSelectedPois(pois: List<PoiType>)
}
