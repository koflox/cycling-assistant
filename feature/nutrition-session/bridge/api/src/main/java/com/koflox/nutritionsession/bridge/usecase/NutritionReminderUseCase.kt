package com.koflox.nutritionsession.bridge.usecase

import kotlinx.coroutines.flow.Flow

interface NutritionReminderUseCase {
    fun observeNutritionReminders(): Flow<Unit>
}
