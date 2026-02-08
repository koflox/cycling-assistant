package com.koflox.nutritionsession.bridge.impl.usecase

import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal class NutritionReminderUseCaseImpl(
    private val observeNutritionEventsUseCase: ObserveNutritionEventsUseCase,
) : NutritionReminderUseCase {

    override fun observeNutritionReminders(): Flow<Unit> =
        observeNutritionEventsUseCase.observeNutritionEvents()
            .filter { it is NutritionEvent.BreakRequired }
            .map { }
}
