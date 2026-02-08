package com.koflox.destinationnutrition.bridge.impl.usecase

import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveNutritionBreakUseCaseImpl(
    private val observeNutritionEventsUseCase: ObserveNutritionEventsUseCase,
) : ObserveNutritionBreakUseCase {

    override fun observeNutritionBreakEvents(): Flow<NutritionBreakEvent> =
        observeNutritionEventsUseCase.observeNutritionEvents().map { event ->
            when (event) {
                is NutritionEvent.BreakRequired -> NutritionBreakEvent.BreakRequired(
                    suggestionTimeMs = event.suggestionTimeMs,
                )
                NutritionEvent.ChecksStopped -> NutritionBreakEvent.ChecksStopped
            }
        }
}
