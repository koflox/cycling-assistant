package com.koflox.session.presentation.sessionslist

import app.cash.turbine.test
import com.koflox.designsystem.text.UiText
import com.koflox.session.domain.usecase.DeleteSessionUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.history.R
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val deleteSessionUseCase: DeleteSessionUseCase = mockk()
    private val mapper: SessionsListUiMapper = mockk()

    private lateinit var viewModel: SessionsListViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { mapper.toUiModel(any()) } returns SessionListItemUiModel(
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
            deleteSessionUseCase = deleteSessionUseCase,
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

    @Test
    fun `DeleteRequested shows DeleteConfirmation overlay`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // initial Content
            viewModel.onEvent(SessionsListUiEvent.DeleteRequested(SESSION_ID))
            val content = awaitItem() as SessionsListUiState.Content
            val overlay = content.overlay as SessionsListOverlay.DeleteConfirmation
            assertEquals(SESSION_ID, overlay.sessionId)
        }
    }

    @Test
    fun `DeleteDismissed clears overlay`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(SessionsListUiEvent.DeleteRequested(SESSION_ID))
            awaitItem() // overlay shown
            viewModel.onEvent(SessionsListUiEvent.DeleteDismissed)
            val content = awaitItem() as SessionsListUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `DeleteConfirmed success shows success Toast and calls use case`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        coEvery { deleteSessionUseCase.delete(SESSION_ID) } returns Result.success(Unit)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(SessionsListUiEvent.DeleteConfirmed(SESSION_ID))
            val content = awaitItem() as SessionsListUiState.Content
            val toast = content.overlay as SessionsListOverlay.Toast
            assertEquals(UiText.Resource(R.string.sessions_list_delete_success), toast.message)
        }
        coVerify { deleteSessionUseCase.delete(SESSION_ID) }
    }

    @Test
    fun `DeleteConfirmed failure shows error Toast`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        coEvery { deleteSessionUseCase.delete(SESSION_ID) } returns Result.failure(RuntimeException("boom"))
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(SessionsListUiEvent.DeleteConfirmed(SESSION_ID))
            val content = awaitItem() as SessionsListUiState.Content
            val toast = content.overlay as SessionsListOverlay.Toast
            assertEquals(UiText.Resource(R.string.sessions_list_delete_error), toast.message)
        }
    }

    @Test
    fun `ToastDismissed clears overlay`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        coEvery { deleteSessionUseCase.delete(SESSION_ID) } returns Result.success(Unit)
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(SessionsListUiEvent.DeleteConfirmed(SESSION_ID))
            awaitItem() // toast shown
            viewModel.onEvent(SessionsListUiEvent.ToastDismissed)
            val content = awaitItem() as SessionsListUiState.Content
            assertNull(content.overlay)
        }
    }
}
