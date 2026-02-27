package com.koflox.poi.domain.usecase

import com.koflox.poi.domain.model.MAX_SELECTED_POIS
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.repository.PoiRepository

internal interface UpdateSelectedPoisUseCase {
    suspend fun updateSelectedPois(pois: List<PoiType>)
}

internal class InvalidPoiSelectionException(message: String) : IllegalArgumentException(message)

internal class UpdateSelectedPoisUseCaseImpl(
    private val repository: PoiRepository,
) : UpdateSelectedPoisUseCase {

    override suspend fun updateSelectedPois(pois: List<PoiType>) {
        if (pois.size != MAX_SELECTED_POIS) {
            throw InvalidPoiSelectionException("Expected $MAX_SELECTED_POIS POIs, got ${pois.size}")
        }
        repository.updateSelectedPois(pois)
    }
}
