package com.koflox.poi.domain.usecase

import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.repository.PoiRepository
import kotlinx.coroutines.flow.Flow

internal interface ObserveSelectedPoisUseCase {
    fun observeSelectedPois(): Flow<List<PoiType>>
}

internal class ObserveSelectedPoisUseCaseImpl(
    private val repository: PoiRepository,
) : ObserveSelectedPoisUseCase {
    override fun observeSelectedPois(): Flow<List<PoiType>> = repository.observeSelectedPois()
}
