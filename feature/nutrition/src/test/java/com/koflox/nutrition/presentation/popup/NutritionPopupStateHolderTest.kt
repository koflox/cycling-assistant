package com.koflox.nutrition.presentation.popup

import app.cash.turbine.test
import com.koflox.testing.coroutine.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NutritionPopupStateHolderTest {

    companion object {
        private const val SUGGESTION_TIME_MS = 1000L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStateHolder(suggestionTimeMs: Long = SUGGESTION_TIME_MS): NutritionPopupStateHolder {
        return NutritionPopupStateHolder(
            suggestionTimeMs = suggestionTimeMs,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state is Hidden`() = runTest(mainDispatcherRule.testDispatcher) {
        val stateHolder = createStateHolder()

        stateHolder.uiState.test {
            assertTrue(awaitItem() is NutritionPopupUiState.Hidden)
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }

    @Test
    fun `state becomes Visible after initialization`() = runTest(mainDispatcherRule.testDispatcher) {
        val stateHolder = createStateHolder()

        stateHolder.uiState.test {
            awaitItem() // Hidden
            val visible = awaitItem()
            assertTrue(visible is NutritionPopupUiState.Visible)
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }

    @Test
    fun `elapsed time is formatted correctly for seconds`() = runTest(mainDispatcherRule.testDispatcher) {
        val currentTime = System.currentTimeMillis()
        val suggestionTime = currentTime - 5000 // 5 seconds ago
        val stateHolder = createStateHolder(suggestionTimeMs = suggestionTime)

        stateHolder.uiState.test {
            awaitItem() // Hidden
            val visible = awaitItem() as NutritionPopupUiState.Visible
            assertTrue(visible.elapsedTimerFormatted.startsWith("00:0"))
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }

    @Test
    fun `elapsed time is formatted correctly for minutes`() = runTest(mainDispatcherRule.testDispatcher) {
        val currentTime = System.currentTimeMillis()
        val suggestionTime = currentTime - 125000 // 2 minutes 5 seconds ago
        val stateHolder = createStateHolder(suggestionTimeMs = suggestionTime)

        stateHolder.uiState.test {
            awaitItem() // Hidden
            val visible = awaitItem() as NutritionPopupUiState.Visible
            assertTrue(visible.elapsedTimerFormatted.startsWith("02:0"))
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }

    @Test
    fun `elapsed time shows 00 00 for current time`() = runTest(mainDispatcherRule.testDispatcher) {
        val currentTime = System.currentTimeMillis()
        val stateHolder = createStateHolder(suggestionTimeMs = currentTime)

        stateHolder.uiState.test {
            awaitItem() // Hidden
            val visible = awaitItem() as NutritionPopupUiState.Visible
            assertEquals("00:00", visible.elapsedTimerFormatted)
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }

    @Test
    fun `negative elapsed time is clamped to zero`() = runTest(mainDispatcherRule.testDispatcher) {
        val futureTime = System.currentTimeMillis() + 10000 // Future time
        val stateHolder = createStateHolder(suggestionTimeMs = futureTime)

        stateHolder.uiState.test {
            awaitItem() // Hidden
            val visible = awaitItem() as NutritionPopupUiState.Visible
            assertEquals("00:00", visible.elapsedTimerFormatted)
            cancelAndIgnoreRemainingEvents()
        }
        stateHolder.dispose()
    }
}
