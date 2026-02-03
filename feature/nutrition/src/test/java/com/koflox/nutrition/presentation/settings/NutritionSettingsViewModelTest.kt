package com.koflox.nutrition.presentation.settings

import app.cash.turbine.test
import com.koflox.nutrition.domain.model.NutritionSettings
import com.koflox.nutrition.domain.usecase.ObserveNutritionSettingsUseCase
import com.koflox.nutrition.domain.usecase.UpdateNutritionSettingsUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NutritionSettingsViewModelTest {

    companion object {
        private const val DEFAULT_INTERVAL = 25
        private const val CUSTOM_INTERVAL = 30
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeNutritionSettingsUseCase: ObserveNutritionSettingsUseCase = mockk()
    private val updateNutritionSettingsUseCase: UpdateNutritionSettingsUseCase = mockk(relaxed = true)
    private val settingsFlow = MutableStateFlow(createSettings())

    private lateinit var viewModel: NutritionSettingsViewModel

    @Before
    fun setup() {
        every { observeNutritionSettingsUseCase.observeSettings() } returns settingsFlow
    }

    private fun createViewModel() = NutritionSettingsViewModel(
        observeNutritionSettingsUseCase = observeNutritionSettingsUseCase,
        updateNutritionSettingsUseCase = updateNutritionSettingsUseCase,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    private fun createSettings(
        isEnabled: Boolean = true,
        intervalMinutes: Int = DEFAULT_INTERVAL,
    ) = NutritionSettings(
        isEnabled = isEnabled,
        intervalMinutes = intervalMinutes,
    )

    @Test
    fun `initial state reflects settings from use case`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(true, state.isEnabled)
            assertEquals(DEFAULT_INTERVAL, state.intervalMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ui state updates when settings change`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            settingsFlow.value = createSettings(isEnabled = false, intervalMinutes = CUSTOM_INTERVAL)

            val updatedState = awaitItem()
            assertEquals(false, updatedState.isEnabled)
            assertEquals(CUSTOM_INTERVAL, updatedState.intervalMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EnabledChanged event calls updateNutritionSettingsUseCase`() = runTest {
        viewModel = createViewModel()

        viewModel.onEvent(NutritionSettingsUiEvent.EnabledChanged(false))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify { updateNutritionSettingsUseCase.setEnabled(false) }
    }

    @Test
    fun `IntervalChanged event calls updateNutritionSettingsUseCase`() = runTest {
        viewModel = createViewModel()

        viewModel.onEvent(NutritionSettingsUiEvent.IntervalChanged(CUSTOM_INTERVAL))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify { updateNutritionSettingsUseCase.setIntervalMinutes(CUSTOM_INTERVAL) }
    }

    @Test
    fun `ui state contains correct slider configuration`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(NutritionSettings.MIN_INTERVAL_MINUTES, state.minIntervalMinutes)
            assertEquals(NutritionSettings.MAX_INTERVAL_MINUTES, state.maxIntervalMinutes)
            assertEquals(NutritionSettings.INTERVAL_STEP_MINUTES, state.intervalStepMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
