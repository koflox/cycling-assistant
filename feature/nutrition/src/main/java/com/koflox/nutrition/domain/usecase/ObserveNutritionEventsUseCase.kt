package com.koflox.nutrition.domain.usecase

import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutritionsession.bridge.model.SessionTimeInfo
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge

interface ObserveNutritionEventsUseCase {
    fun observeNutritionEvents(): Flow<NutritionEvent>
}

internal class ObserveNutritionEventsUseCaseImpl(
    private val sessionElapsedTimeUseCase: SessionElapsedTimeUseCase,
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
    private val intervalMs: Long = NUTRITION_INTERVAL_MS,
    private val checkIntervalMs: Long = CHECK_INTERVAL_MS,
) : ObserveNutritionEventsUseCase {

    companion object {
        private const val NUTRITION_INTERVAL_MS = 25L * 60L * 1000L
        private const val CHECK_INTERVAL_MS = 1000L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("AssignedValueIsNeverRead")
    override fun observeNutritionEvents(): Flow<NutritionEvent> = flow {
        var lastEmittedInterval = 0
        var hadSession = false

        sessionElapsedTimeUseCase.observeSessionTimeInfo()
            .flatMapLatest { timeInfo ->
                when {
                    timeInfo == null -> flowOf(NutritionCheckResult.SessionEnded)
                    !timeInfo.isRunning -> flowOf(NutritionCheckResult.SessionPaused)
                    else -> createTickerFlow(timeInfo)
                }
            }
            .collect { result ->
                when (result) {
                    is NutritionCheckResult.SessionEnded -> {
                        if (hadSession) {
                            emit(NutritionEvent.ChecksStopped)
                            lastEmittedInterval = 0
                            hadSession = false
                        }
                    }
                    is NutritionCheckResult.SessionPaused -> {
                        hadSession = true
                    }
                    is NutritionCheckResult.TimeCheck -> {
                        hadSession = true
                        val currentInterval = calculateCurrentInterval(result.timeInfo)
                        if (currentInterval > lastEmittedInterval) {
                            emit(
                                NutritionEvent.BreakRequired(
                                    suggestionTimeMs = currentTimeProvider(),
                                    intervalNumber = currentInterval,
                                ),
                            )
                            lastEmittedInterval = currentInterval
                        }
                    }
                }
            }
    }

    private fun createTickerFlow(timeInfo: SessionTimeInfo): Flow<NutritionCheckResult> =
        merge(
            flowOf(NutritionCheckResult.TimeCheck(timeInfo)),
            flow {
                while (true) {
                    delay(checkIntervalMs)
                    emit(NutritionCheckResult.TimeCheck(timeInfo))
                }
            },
        )

    private fun calculateCurrentInterval(timeInfo: SessionTimeInfo): Int {
        val currentTimeMs = currentTimeProvider()
        val realElapsedMs = timeInfo.elapsedTimeMs + (currentTimeMs - timeInfo.lastResumedTimeMs)
        return (realElapsedMs / intervalMs).toInt()
    }

    private sealed interface NutritionCheckResult {
        data object SessionEnded : NutritionCheckResult
        data object SessionPaused : NutritionCheckResult
        data class TimeCheck(val timeInfo: SessionTimeInfo) : NutritionCheckResult
    }
}
