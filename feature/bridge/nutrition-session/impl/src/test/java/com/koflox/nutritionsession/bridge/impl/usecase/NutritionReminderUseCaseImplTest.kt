package com.koflox.nutritionsession.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NutritionReminderUseCaseImplTest {

    companion object {
        private const val SUGGESTION_TIME_MS = 1500000L
        private const val INTERVAL_NUMBER = 1
    }

    private val observeNutritionEventsUseCase: ObserveNutritionEventsUseCase = mockk()
    private lateinit var useCase: NutritionReminderUseCaseImpl

    @Before
    fun setup() {
        useCase = NutritionReminderUseCaseImpl(
            observeNutritionEventsUseCase = observeNutritionEventsUseCase,
        )
    }

    @Test
    fun `observeNutritionReminders emits for BreakRequired events`() = runTest {
        every { observeNutritionEventsUseCase.observeNutritionEvents() } returns flowOf(
            NutritionEvent.BreakRequired(
                suggestionTimeMs = SUGGESTION_TIME_MS,
                intervalNumber = INTERVAL_NUMBER,
            ),
        )

        useCase.observeNutritionReminders().test {
            awaitItem()
            awaitComplete()
        }
    }

    @Test
    fun `observeNutritionReminders filters out ChecksStopped events`() = runTest {
        every { observeNutritionEventsUseCase.observeNutritionEvents() } returns flowOf(
            NutritionEvent.ChecksStopped,
        )

        useCase.observeNutritionReminders().test {
            awaitComplete()
        }
    }

    @Test
    fun `observeNutritionReminders emits only for BreakRequired in mixed events`() = runTest {
        every { observeNutritionEventsUseCase.observeNutritionEvents() } returns flowOf(
            NutritionEvent.ChecksStopped,
            NutritionEvent.BreakRequired(
                suggestionTimeMs = SUGGESTION_TIME_MS,
                intervalNumber = INTERVAL_NUMBER,
            ),
            NutritionEvent.ChecksStopped,
        )

        useCase.observeNutritionReminders().test {
            awaitItem()
            awaitComplete()
        }
    }
}
