package com.koflox.poi.presentation.selection

import app.cash.turbine.test
import com.koflox.poi.domain.model.MAX_SELECTED_POIS
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PoiSelectionViewModelTest {

    companion object {
        private val DEFAULT_SELECTION = listOf(PoiType.COFFEE_SHOP, PoiType.TOILET)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSelectedPoisUseCase: ObserveSelectedPoisUseCase = mockk()
    private val updateSelectedPoisUseCase: UpdateSelectedPoisUseCase = mockk()
    private lateinit var viewModel: PoiSelectionViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { observeSelectedPoisUseCase.observeSelectedPois() } returns flowOf(DEFAULT_SELECTION)
        coEvery { updateSelectedPoisUseCase.updateSelectedPois(any()) } returns Unit
    }

    private fun createViewModel() = PoiSelectionViewModel(
        observeSelectedPoisUseCase = observeSelectedPoisUseCase,
        updateSelectedPoisUseCase = updateSelectedPoisUseCase,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is PoiSelectionUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadData emits Content with selected and available pois`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            assertEquals(DEFAULT_SELECTION.size, content.selectedPois.size)
            assertEquals(DEFAULT_SELECTION, content.selectedPois.map { it.type })
            assertEquals(PoiType.entries.size - DEFAULT_SELECTION.size, content.availablePois.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing selected poi moves it to available`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertFalse(content.selectedPois.any { it.type == PoiType.COFFEE_SHOP })
            assertTrue(content.availablePois.any { it.type == PoiType.COFFEE_SHOP })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding available poi moves it to selected`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            awaitItem() // Removed COFFEE_SHOP
            viewModel.onEvent(PoiSelectionUiEvent.PoiAdded(PoiType.PARK))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertTrue(content.selectedPois.any { it.type == PoiType.PARK })
            assertFalse(content.availablePois.any { it.type == PoiType.PARK })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cannot add more than MAX_SELECTED_POIS`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content (2 selected = MAX)
            viewModel.onEvent(PoiSelectionUiEvent.PoiAdded(PoiType.PARK))
            testScheduler.advanceUntilIdle()
            val content = viewModel.uiState.value as PoiSelectionUiState.Content
            assertEquals(MAX_SELECTED_POIS, content.selectedPois.size)
            assertFalse(content.selectedPois.any { it.type == PoiType.PARK })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save enabled when exactly 2 selected and different from saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            awaitItem() // Removed COFFEE_SHOP (1 selected)
            viewModel.onEvent(PoiSelectionUiEvent.PoiAdded(PoiType.PARK))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertTrue(content.isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save disabled when selection equals saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            assertFalse(content.isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save calls updateSelectedPoisUseCase and navigates back`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            awaitItem() // Removed
            viewModel.onEvent(PoiSelectionUiEvent.PoiAdded(PoiType.PARK))
            awaitItem() // New selection
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.navigation.test {
            viewModel.onEvent(PoiSelectionUiEvent.SaveClicked)
            assertEquals(PoiSelectionNavigation.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { updateSelectedPoisUseCase.updateSelectedPois(any()) }
    }

    @Test
    fun `selected pois maintain correct order`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            assertEquals(PoiType.COFFEE_SHOP, content.selectedPois[0].type)
            assertEquals(PoiType.TOILET, content.selectedPois[1].type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing first poi keeps remaining poi as first`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertEquals(1, content.selectedPois.size)
            assertEquals(PoiType.TOILET, content.selectedPois[0].type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save preserves selection order`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            awaitItem() // Removed COFFEE_SHOP
            viewModel.onEvent(PoiSelectionUiEvent.PoiAdded(PoiType.PARK))
            awaitItem() // Added PARK
            cancelAndIgnoreRemainingEvents()
        }
        val savedPois = slot<List<PoiType>>()
        coEvery { updateSelectedPoisUseCase.updateSelectedPois(capture(savedPois)) } returns Unit
        viewModel.navigation.test {
            viewModel.onEvent(PoiSelectionUiEvent.SaveClicked)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(PoiType.TOILET, PoiType.PARK), savedPois.captured)
    }

    @Test
    fun `reordering swaps items in selected list`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val initial = awaitItem() as PoiSelectionUiState.Content
            assertEquals(PoiType.COFFEE_SHOP, initial.selectedPois[0].type)
            assertEquals(PoiType.TOILET, initial.selectedPois[1].type)
            viewModel.onEvent(PoiSelectionUiEvent.PoiReordered(fromIndex = 0, toIndex = 1))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertEquals(PoiType.TOILET, content.selectedPois[0].type)
            assertEquals(PoiType.COFFEE_SHOP, content.selectedPois[1].type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reordering enables save when order differs from saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val initial = awaitItem() as PoiSelectionUiState.Content
            assertFalse(initial.isSaveEnabled)
            viewModel.onEvent(PoiSelectionUiEvent.PoiReordered(fromIndex = 0, toIndex = 1))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertTrue(content.isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add disabled when max pois selected`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            assertFalse(content.isAddEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add enabled when below max pois`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiRemoved(PoiType.COFFEE_SHOP))
            val content = awaitItem() as PoiSelectionUiState.Content
            assertTrue(content.isAddEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
