package com.koflox.session.service

import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal interface NutritionReminderManager {
    fun start(scope: CoroutineScope, onReminder: () -> Unit)
    fun stop()
}

internal class NutritionReminderManagerImpl(
    private val nutritionReminderUseCase: NutritionReminderUseCase,
) : NutritionReminderManager {

    private var job: Job? = null

    override fun start(scope: CoroutineScope, onReminder: () -> Unit) {
        if (job?.isActive == true) return
        job = scope.launch {
            nutritionReminderUseCase.observeNutritionReminders().collect {
                onReminder()
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}
