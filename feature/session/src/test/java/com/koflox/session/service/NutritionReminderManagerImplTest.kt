package com.koflox.session.service

import com.koflox.nutritionsession.bridge.usecase.NutritionReminderUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionReminderManagerImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val nutritionReminderUseCase: NutritionReminderUseCase = mockk()
    private val onReminder: () -> Unit = mockk(relaxed = true)

    private val nutritionFlow = MutableSharedFlow<Unit>()

    private lateinit var manager: NutritionReminderManagerImpl

    @Before
    fun setup() {
        every { nutritionReminderUseCase.observeNutritionReminders() } returns nutritionFlow
        manager = NutritionReminderManagerImpl(
            nutritionReminderUseCase = nutritionReminderUseCase,
        )
    }

    @Test
    fun `start observes nutrition reminders`() = runManagerTest {
        manager.start(this, onReminder)
        advanceTimeBy(1)

        verify { nutritionReminderUseCase.observeNutritionReminders() }
    }

    @Test
    fun `reminder emission triggers onReminder callback`() = runManagerTest {
        manager.start(this, onReminder)
        advanceTimeBy(1)

        nutritionFlow.emit(Unit)
        advanceTimeBy(1)

        verify(exactly = 1) { onReminder() }
    }

    @Test
    fun `multiple reminders trigger multiple callbacks`() = runManagerTest {
        manager.start(this, onReminder)
        advanceTimeBy(1)

        repeat(3) {
            nutritionFlow.emit(Unit)
            advanceTimeBy(1)
        }

        verify(exactly = 3) { onReminder() }
    }

    @Test
    fun `stop prevents further callbacks`() = runManagerTest {
        manager.start(this, onReminder)
        advanceTimeBy(1)

        nutritionFlow.emit(Unit)
        advanceTimeBy(1)

        manager.stop()

        verify(exactly = 1) { onReminder() }
    }

    @Test
    fun `start is idempotent when already active`() = runManagerTest {
        manager.start(this, onReminder)
        manager.start(this, onReminder)
        advanceTimeBy(1)

        nutritionFlow.emit(Unit)
        advanceTimeBy(1)

        verify(exactly = 1) { onReminder() }
    }

    private fun runManagerTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            manager.stop()
        }
    }
}
