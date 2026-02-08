package com.koflox.destinationnutrition.bridge.usecase

import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import kotlinx.coroutines.flow.Flow

interface ObserveNutritionBreakUseCase {
    fun observeNutritionBreakEvents(): Flow<NutritionBreakEvent>
}
