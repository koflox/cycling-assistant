package com.koflox.session.presentation.sessionslist

import app.cash.turbine.test
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionListItemUiModel
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionsListViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Destination"
        private const val FORMATTED_DATE = "Jan 1, 2024"
        private const val FORMATTED_DISTANCE = "15.5 km"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAllSessionsUseCase: GetAllSessionsUseCase = mockk()
    private val mapper: SessionsListUiMapper = mockk()

    private lateinit var viewModel: SessionsListViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { mapper.toUiModel(any()) } returns createSessionListItemUiModel(
            id = SESSION_ID,
            destinationName = DESTINATION_NAME,
            dateFormatted = FORMATTED_DATE,
            distanceFormatted = FORMATTED_DISTANCE,
            status = SessionListItemStatus.COMPLETED,
            isShareButtonVisible = true,
        )
    }

    private fun createViewModel(): SessionsListViewModel {
        return SessionsListViewModel(
            getAllSessionsUseCase = getAllSessionsUseCase,
            mapper = mapper,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is SessionsListUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty sessions list shows Empty state`() = runTest {
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            assertEquals(SessionsListUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `sessions list shows Content state`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionsListUiState.Content
            assertEquals(1, content.sessions.size)
            assertNull(content.overlay)
        }
    }
}
