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
import org.junit.Assert.assertNull
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
    fun `loadData emits Content with all pois and saved selection`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            assertEquals(PoiType.entries.size, content.pois.size)
            val selectedTypes = content.pois.filter { it.isSelected }.map { it.type }
            assertEquals(DEFAULT_SELECTION, selectedTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle selected poi deselects it`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            val content = awaitItem() as PoiSelectionUiState.Content
            val coffeeShop = content.pois.first { it.type == PoiType.COFFEE_SHOP }
            assertFalse(coffeeShop.isSelected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle unselected poi selects it`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            awaitItem() // Deselected COFFEE_SHOP
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.PARK))
            val content = awaitItem() as PoiSelectionUiState.Content
            val park = content.pois.first { it.type == PoiType.PARK }
            assertTrue(park.isSelected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cannot select more than MAX_SELECTED_POIS`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val initialContent = awaitItem() as PoiSelectionUiState.Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.PARK))
            testScheduler.advanceUntilIdle()
            // StateFlow won't re-emit identical value, so verify current state
            val content = viewModel.uiState.value as PoiSelectionUiState.Content
            val selectedCount = content.pois.count { it.isSelected }
            assertEquals(MAX_SELECTED_POIS, selectedCount)
            val park = content.pois.first { it.type == PoiType.PARK }
            assertFalse(park.isSelected)
            assertEquals(initialContent, content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save enabled when exactly 2 selected and different from saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            awaitItem() // Deselected COFFEE_SHOP (1 selected)
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.PARK))
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
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            awaitItem() // Deselected
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.PARK))
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
    fun `selected pois have correct selection indices`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as PoiSelectionUiState.Content
            val coffeeShop = content.pois.first { it.type == PoiType.COFFEE_SHOP }
            val toilet = content.pois.first { it.type == PoiType.TOILET }
            val park = content.pois.first { it.type == PoiType.PARK }
            assertEquals(1, coffeeShop.selectionIndex)
            assertEquals(2, toilet.selectionIndex)
            assertNull(park.selectionIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deselecting first poi shifts second index`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            val content = awaitItem() as PoiSelectionUiState.Content
            val toilet = content.pois.first { it.type == PoiType.TOILET }
            assertEquals(1, toilet.selectionIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save preserves selection order`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.COFFEE_SHOP))
            awaitItem() // Deselected COFFEE_SHOP
            viewModel.onEvent(PoiSelectionUiEvent.PoiToggled(PoiType.PARK))
            awaitItem() // Selected PARK
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
}
