package com.koflox.nutrition.domain.usecase

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutrition.domain.model.NutritionSettings
import com.koflox.nutritionsession.bridge.model.SessionTimeInfo
import com.koflox.nutritionsession.bridge.usecase.SessionElapsedTimeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ObserveNutritionEventsUseCase {
    fun observeNutritionEvents(): Flow<NutritionEvent>
}

internal class ObserveNutritionEventsUseCaseImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val sessionElapsedTimeUseCase: SessionElapsedTimeUseCase,
    private val observeNutritionSettingsUseCase: ObserveNutritionSettingsUseCase,
    private val currentTimeProvider: CurrentTimeProvider,
    private val checkInterval: Duration = CHECK_INTERVAL,
) : ObserveNutritionEventsUseCase {

    companion object {
        private val CHECK_INTERVAL = 30.seconds
        private const val MINUTES_TO_MS = 60L * 1000L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("AssignedValueIsNeverRead")
    override fun observeNutritionEvents(): Flow<NutritionEvent> = flow {
        var lastEmittedInterval = 0
        var hadSession = false

        combine(
            sessionElapsedTimeUseCase.observeSessionTimeInfo(),
            observeNutritionSettingsUseCase.observeSettings(),
        ) { timeInfo, settings ->
            Pair(timeInfo, settings)
        }
            .flatMapLatest { (timeInfo, settings) ->
                when {
                    timeInfo == null -> flowOf(NutritionCheckResult.SessionEnded)
                    !timeInfo.isRunning -> flowOf(NutritionCheckResult.SessionPaused)
                    !settings.isEnabled -> flowOf(NutritionCheckResult.Disabled)
                    else -> createTickerFlow(timeInfo, settings)
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

                    is NutritionCheckResult.Disabled -> {
                        hadSession = true
                    }

                    is NutritionCheckResult.TimeCheck -> {
                        hadSession = true
                        val intervalMs = result.settings.intervalMinutes * MINUTES_TO_MS
                        val currentInterval = calculateCurrentInterval(result.timeInfo, intervalMs)
                        if (currentInterval > lastEmittedInterval) {
                            emit(
                                NutritionEvent.BreakRequired(
                                    suggestionTimeMs = currentTimeProvider.currentTimeMs(),
                                    intervalNumber = currentInterval,
                                ),
                            )
                            lastEmittedInterval = currentInterval
                        }
                    }
                }
            }
    }.flowOn(dispatcherIo)

    private fun createTickerFlow(
        timeInfo: SessionTimeInfo,
        settings: NutritionSettings,
    ): Flow<NutritionCheckResult> = merge(
        flowOf(NutritionCheckResult.TimeCheck(timeInfo, settings)),
        flow {
            while (true) {
                delay(checkInterval)
                emit(NutritionCheckResult.TimeCheck(timeInfo, settings))
            }
        },
    )

    private fun calculateCurrentInterval(timeInfo: SessionTimeInfo, intervalMs: Long): Int {
        val currentTimeMs = currentTimeProvider.currentTimeMs()
        val realElapsedMs = timeInfo.elapsedTimeMs + (currentTimeMs - timeInfo.lastResumedTimeMs)
        return (realElapsedMs / intervalMs).toInt()
    }

    private sealed interface NutritionCheckResult {
        data object SessionEnded : NutritionCheckResult
        data object SessionPaused : NutritionCheckResult
        data object Disabled : NutritionCheckResult
        data class TimeCheck(val timeInfo: SessionTimeInfo, val settings: NutritionSettings) : NutritionCheckResult
    }
}
