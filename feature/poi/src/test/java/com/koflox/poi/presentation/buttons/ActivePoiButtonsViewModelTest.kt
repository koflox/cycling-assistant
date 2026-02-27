package com.koflox.poi.presentation.buttons

import app.cash.turbine.test
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActivePoiButtonsViewModelTest {

    companion object {
        private val DEFAULT_POIS = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
        private val UPDATED_POIS = listOf(PoiType.PARK, PoiType.PHARMACY)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSelectedPoisUseCase: ObserveSelectedPoisUseCase = mockk()

    private fun createViewModel() = ActivePoiButtonsViewModel(
        observeSelectedPoisUseCase = observeSelectedPoisUseCase,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is ActivePoiButtonsUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observing pois emits Content`() = runTest {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as ActivePoiButtonsUiState.Content
            assertEquals(DEFAULT_POIS, content.selectedPois)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pois update triggers new Content`() = runTest {
        val poisFlow = MutableStateFlow(DEFAULT_POIS)
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns poisFlow
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val first = awaitItem() as ActivePoiButtonsUiState.Content
            assertEquals(DEFAULT_POIS, first.selectedPois)
            poisFlow.value = UPDATED_POIS
            val second = awaitItem() as ActivePoiButtonsUiState.Content
            assertEquals(UPDATED_POIS, second.selectedPois)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
