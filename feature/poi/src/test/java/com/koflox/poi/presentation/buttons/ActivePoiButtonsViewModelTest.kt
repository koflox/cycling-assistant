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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActivePoiButtonsViewModelTest {

    companion object {
        private val DEFAULT_POIS = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
        private val UPDATED_POIS = listOf(PoiType.PARK, PoiType.PHARMACY)
        private val DEFAULT_UNSELECTED_POIS = PoiType.entries - DEFAULT_POIS.toSet()
        private val UPDATED_UNSELECTED_POIS = PoiType.entries - UPDATED_POIS.toSet()
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
    fun `observing pois emits Content with correct unselected pois`() = runTest {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as ActivePoiButtonsUiState.Content
            assertEquals(DEFAULT_UNSELECTED_POIS, content.unselectedPois)
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
            assertEquals(UPDATED_UNSELECTED_POIS, second.unselectedPois)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `MoreClicked sets isMoreDialogVisible true`() = runTest {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(ActivePoiButtonsUiEvent.MoreClicked)
            val content = awaitItem() as ActivePoiButtonsUiState.Content
            assertTrue(content.isMoreDialogVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `MoreDialogDismissed sets isMoreDialogVisible false`() = runTest {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_POIS)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(ActivePoiButtonsUiEvent.MoreClicked)
            awaitItem() // isMoreDialogVisible = true
            viewModel.onEvent(ActivePoiButtonsUiEvent.MoreDialogDismissed)
            val content = awaitItem() as ActivePoiButtonsUiState.Content
            assertFalse(content.isMoreDialogVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
