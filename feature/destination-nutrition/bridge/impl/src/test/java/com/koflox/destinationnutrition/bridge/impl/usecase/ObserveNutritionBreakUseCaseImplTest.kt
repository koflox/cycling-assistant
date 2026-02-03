package com.koflox.destinationnutrition.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import com.koflox.nutrition.domain.model.NutritionEvent
import com.koflox.nutrition.domain.usecase.ObserveNutritionEventsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveNutritionBreakUseCaseImplTest {

    companion object {
        private const val SUGGESTION_TIME_MS = 1234567890L
    }

    private val observeNutritionEventsUseCase: ObserveNutritionEventsUseCase = mockk()
    private val nutritionEventsFlow = MutableSharedFlow<NutritionEvent>()

    private lateinit var useCase: ObserveNutritionBreakUseCaseImpl

    @Before
    fun setup() {
        every { observeNutritionEventsUseCase.observeNutritionEvents() } returns nutritionEventsFlow
        useCase = ObserveNutritionBreakUseCaseImpl(observeNutritionEventsUseCase)
    }

    @Test
    fun `maps BreakRequired event correctly`() = runTest {
        useCase.observeNutritionBreakEvents().test {
            nutritionEventsFlow.emit(
                NutritionEvent.BreakRequired(
                    suggestionTimeMs = SUGGESTION_TIME_MS,
                    intervalNumber = 1,
                ),
            )

            val event = awaitItem()
            assertTrue(event is NutritionBreakEvent.BreakRequired)
            assertEquals(SUGGESTION_TIME_MS, (event as NutritionBreakEvent.BreakRequired).suggestionTimeMs)
        }
    }

    @Test
    fun `maps ChecksStopped event correctly`() = runTest {
        useCase.observeNutritionBreakEvents().test {
            nutritionEventsFlow.emit(NutritionEvent.ChecksStopped)

            val event = awaitItem()
            assertEquals(NutritionBreakEvent.ChecksStopped, event)
        }
    }

    @Test
    fun `maps multiple events in sequence`() = runTest {
        useCase.observeNutritionBreakEvents().test {
            nutritionEventsFlow.emit(
                NutritionEvent.BreakRequired(
                    suggestionTimeMs = SUGGESTION_TIME_MS,
                    intervalNumber = 1,
                ),
            )
            val event1 = awaitItem()
            assertTrue(event1 is NutritionBreakEvent.BreakRequired)

            nutritionEventsFlow.emit(NutritionEvent.ChecksStopped)
            val event2 = awaitItem()
            assertEquals(NutritionBreakEvent.ChecksStopped, event2)
        }
    }
}
