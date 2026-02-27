package com.koflox.poi.domain.repository

import com.koflox.poi.domain.model.PoiType
import kotlinx.coroutines.flow.Flow

internal interface PoiRepository {
    fun observeSelectedPois(): Flow<List<PoiType>>
    suspend fun updateSelectedPois(pois: List<PoiType>)
}
