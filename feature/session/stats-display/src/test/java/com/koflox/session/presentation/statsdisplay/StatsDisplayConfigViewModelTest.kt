package com.koflox.session.presentation.statsdisplay

import app.cash.turbine.test
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.domain.usecase.UpdateStatsDisplayConfigUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StatsDisplayConfigViewModelTest {

    companion object {
        private val DEFAULT_ACTIVE = StatsDisplayConfig.DEFAULT_ACTIVE_SESSION_STATS
        private val DEFAULT_COMPLETED = StatsDisplayConfig.DEFAULT_COMPLETED_SESSION_STATS
        private val DEFAULT_SHARE = StatsDisplayConfig.DEFAULT_SHARE_STATS
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeUseCase: ObserveStatsDisplayConfigUseCase = mockk()
    private val updateUseCase: UpdateStatsDisplayConfigUseCase = mockk()
    private lateinit var viewModel: StatsDisplayConfigViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { observeUseCase.observeStatsDisplayConfig() } returns flowOf(StatsDisplayConfig.DEFAULT)
        coEvery { updateUseCase.updateActiveSessionStats(any()) } returns Unit
        coEvery { updateUseCase.updateCompletedSessionStats(any()) } returns Unit
        coEvery { updateUseCase.updateShareStats(any()) } returns Unit
    }

    private fun createViewModel() = StatsDisplayConfigViewModel(
        observeStatsDisplayConfigUseCase = observeUseCase,
        updateStatsDisplayConfigUseCase = updateUseCase,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    private fun StatsDisplayConfigUiState.Content.activeSection() =
        sections.first { it.section == StatsDisplaySection.ACTIVE_SESSION }

    private fun StatsDisplayConfigUiState.Content.completedSection() =
        sections.first { it.section == StatsDisplaySection.COMPLETED_SESSION }

    private fun StatsDisplayConfigUiState.Content.shareSection() =
        sections.first { it.section == StatsDisplaySection.SHARE }

    // region Initialization

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is StatsDisplayConfigUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadData emits Content with default config`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertEquals(3, content.sections.size)
            assertEquals(DEFAULT_ACTIVE.size, content.activeSection().selectedStats.size)
            assertEquals(DEFAULT_COMPLETED.size, content.completedSection().selectedStats.size)
            assertEquals(DEFAULT_SHARE.size, content.shareSection().selectedStats.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `active section has correct selected and available split`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            val active = content.activeSection()
            assertEquals(DEFAULT_ACTIVE.map { it.name }, active.selectedStats.map { it.type.name })
            val expectedAvailable = StatsDisplayConfig.ACTIVE_SESSION_POOL.filter { it !in DEFAULT_ACTIVE }
            assertEquals(expectedAvailable.size, active.availableStats.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Add / Remove

    @Test
    fun `adding stat to active section moves it to selected`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            // Remove one first to make room (active is exactly 5)
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.TIME))
            awaitItem()
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatAdded(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.AVG_POWER))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.activeSection().selectedStats.any { it.type == SessionStatType.AVG_POWER })
            assertFalse(content.activeSection().availableStats.any { it.type == SessionStatType.AVG_POWER })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing stat from active section moves it to available`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.TIME))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.activeSection().selectedStats.any { it.type == SessionStatType.TIME })
            assertTrue(content.activeSection().availableStats.any { it.type == SessionStatType.TIME })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cannot add beyond max for active section`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content (active has 5 = max)
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatAdded(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.AVG_POWER))
            testScheduler.advanceUntilIdle()
            val content = viewModel.uiState.value as StatsDisplayConfigUiState.Content
            assertEquals(StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT, content.activeSection().selectedStats.size)
            assertFalse(content.activeSection().selectedStats.any { it.type == SessionStatType.AVG_POWER })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completed section has no max limit`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            val extraStat = SessionStatType.CALORIES_WEIGHT
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatAdded(StatsDisplaySection.COMPLETED_SESSION, extraStat))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.completedSection().selectedStats.any { it.type == extraStat })
            assertNull(content.completedSection().maxSelectionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Reorder

    @Test
    fun `reordering swaps items in active section`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val initial = awaitItem() as StatsDisplayConfigUiState.Content
            val firstType = initial.activeSection().selectedStats[0].type
            val secondType = initial.activeSection().selectedStats[1].type
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertEquals(secondType, content.activeSection().selectedStats[0].type)
            assertEquals(firstType, content.activeSection().selectedStats[1].type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reordering enables save when order differs from saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val initial = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(initial.activeSection().isSaveEnabled)
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.activeSection().isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Validation

    @Test
    fun `active section invalid when not exactly 5 stats`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Content
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.TIME))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.activeSection().isSelectionValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completed section valid when at least 4 stats`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.completedSection().isSelectionValid)
            assertTrue(content.completedSection().selectedStats.size >= StatsDisplayConfig.COMPLETED_SESSION_MIN_STATS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `share section invalid when below 4 stats`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial (share has 4 = min)
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.SHARE, SessionStatType.TIME))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.shareSection().isSelectionValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isAddEnabled false when active section at max`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.activeSection().isAddEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isAddEnabled true when active section below max`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.TIME))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.activeSection().isAddEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isAddEnabled always true for completed section`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.completedSection().isAddEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Save

    @Test
    fun `save disabled when selection equals saved`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.activeSection().isSaveEnabled)
            assertFalse(content.completedSection().isSaveEnabled)
            assertFalse(content.shareSection().isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save section calls update and keeps screen`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            awaitItem() // Reordered
            viewModel.onEvent(StatsDisplayConfigUiEvent.SaveSectionClicked(StatsDisplaySection.ACTIVE_SESSION))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.activeSection().isSaveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { updateUseCase.updateActiveSessionStats(any()) }
    }

    @Test
    fun `save all calls all updates and navigates back`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            awaitItem() // Dirty
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.navigation.test {
            viewModel.onEvent(StatsDisplayConfigUiEvent.SaveAllClicked)
            assertEquals(StatsDisplayConfigNavigation.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { updateUseCase.updateActiveSessionStats(any()) }
        coVerify { updateUseCase.updateCompletedSessionStats(any()) }
        coVerify { updateUseCase.updateShareStats(any()) }
    }

    @Test
    fun `save all disabled when no section is dirty`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.isSaveAllEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save all enabled when sections valid and at least one dirty`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertTrue(content.isSaveAllEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save all disabled when any section is invalid`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            // Make active invalid (remove one stat, goes from 5 to 4)
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatRemoved(StatsDisplaySection.ACTIVE_SESSION, SessionStatType.TIME))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertFalse(content.isSaveAllEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Reset

    @Test
    fun `reset section restores default stats`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial
            viewModel.onEvent(StatsDisplayConfigUiEvent.StatReordered(StatsDisplaySection.ACTIVE_SESSION, 0, 1))
            awaitItem() // Reordered
            viewModel.onEvent(StatsDisplayConfigUiEvent.ResetSectionClicked(StatsDisplaySection.ACTIVE_SESSION))
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            val expectedTypes = StatsDisplayConfig.DEFAULT_ACTIVE_SESSION_STATS.map { it.name }
            assertEquals(expectedTypes, content.activeSection().selectedStats.map { it.type.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Counter / Max Selection

    @Test
    fun `active section has correct maxSelectionCount`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertEquals(StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT, content.activeSection().maxSelectionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `share section has correct maxSelectionCount`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertEquals(StatsDisplayConfig.SHARE_MAX_STATS, content.shareSection().maxSelectionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completed section has null maxSelectionCount`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as StatsDisplayConfigUiState.Content
            assertNull(content.completedSection().maxSelectionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion
}
