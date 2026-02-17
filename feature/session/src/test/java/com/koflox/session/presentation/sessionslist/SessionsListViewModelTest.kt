package com.koflox.session.presentation.sessionslist

import android.content.Intent
import android.graphics.Bitmap
import app.cash.turbine.test
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetAllSessionsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.ShareResult
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionListItemUiModel
import com.koflox.session.testutil.createSessionUiModel
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
        private const val SHARE_TEXT = "Check out my ride!"
        private const val CHOOSER_TITLE = "Share via"
        private val SHARE_ERROR_UI_TEXT = UiText.Resource(com.koflox.session.R.string.share_image_processing_error)
        private val LOAD_ERROR_UI_TEXT = UiText.Resource(com.koflox.error.R.string.error_not_handled)
        private const val FORMATTED_DATE = "Jan 1, 2024"
        private const val FORMATTED_TIME = "01:30:00"
        private const val FORMATTED_DISTANCE = "15.5 km"
        private const val FORMATTED_AVG_SPEED = "22.0 km/h"
        private const val FORMATTED_TOP_SPEED = "35.0 km/h"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAllSessionsUseCase: GetAllSessionsUseCase = mockk()
    private val getSessionByIdUseCase: GetSessionByIdUseCase = mockk()
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase = mockk()
    private val mapper: SessionsListUiMapper = mockk()
    private val sessionUiMapper: SessionUiMapper = mockk()
    private val imageSharer: SessionImageSharer = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
    private val shareErrorMapper: ShareErrorMapper = mockk()

    private lateinit var viewModel: SessionsListViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { sessionUiMapper.toSessionUiModel(any()) } returns createSessionUiModel(
            elapsedTimeFormatted = FORMATTED_TIME,
            traveledDistanceFormatted = FORMATTED_DISTANCE,
            averageSpeedFormatted = FORMATTED_AVG_SPEED,
            topSpeedFormatted = FORMATTED_TOP_SPEED,
        )
        every { sessionUiMapper.formatStartDate(any()) } returns FORMATTED_DATE
        every { sessionUiMapper.formatElapsedTime(any()) } returns "00:00:00"
        every { sessionUiMapper.formatAltitudeGain(any()) } returns "0"
        every { sessionUiMapper.formatCalories(any()) } returns "0"
        coEvery { calculateSessionStatsUseCase.calculate(any()) } returns Result.success(
            SessionDerivedStats(
                idleTimeMs = 0L,
                movingTimeMs = 0L,
                altitudeLossMeters = 0.0,
                caloriesBurned = 0.0,
            ),
        )
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
            getSessionByIdUseCase = getSessionByIdUseCase,
            calculateSessionStatsUseCase = calculateSessionStatsUseCase,
            mapper = mapper,
            sessionUiMapper = sessionUiMapper,
            imageSharer = imageSharer,
            errorMessageMapper = errorMessageMapper,
            shareErrorMapper = shareErrorMapper,
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
    fun `ShareClicked shows share preview`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))

            val updatedContent = awaitItem() as SessionsListUiState.Content
            assertTrue(updatedContent.overlay is SessionsListOverlay.SharePreview)
            val preview = updatedContent.overlay as SessionsListOverlay.SharePreview
            assertEquals(SESSION_ID, preview.data.sessionId)
            assertEquals(DESTINATION_NAME, preview.data.destinationName)
        }
    }

    @Test
    fun `ShareDialogDismissed clears overlay`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareDialogDismissed)

            val content = awaitItem() as SessionsListUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `ShareConfirmed shows Sharing state first`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))

            val sharingState = awaitItem() as SessionsListUiState.Content
            assertTrue(sharingState.overlay is SessionsListOverlay.Sharing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShareConfirmed success shows ShareReady`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))

            awaitItem() // Sharing
            val readyState = awaitItem() as SessionsListUiState.Content
            assertTrue(readyState.overlay is SessionsListOverlay.ShareReady)
            assertEquals(intent, (readyState.overlay as SessionsListOverlay.ShareReady).intent)
        }
    }

    @Test
    fun `ShareConfirmed failure shows ShareError`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        val bitmap = mockk<Bitmap>()
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.CannotProcessTheImage
        every { shareErrorMapper.map(ShareResult.CannotProcessTheImage) } returns SHARE_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))

            awaitItem() // Sharing
            val errorState = awaitItem() as SessionsListUiState.Content
            assertTrue(errorState.overlay is SessionsListOverlay.ShareError)
            assertEquals(SHARE_ERROR_UI_TEXT, (errorState.overlay as SessionsListOverlay.ShareError).message)
        }
    }

    @Test
    fun `ShareIntentLaunched clears overlay`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))
            awaitItem() // Sharing
            awaitItem() // ShareReady

            viewModel.onEvent(SessionsListUiEvent.ShareIntentLaunched)

            val content = awaitItem() as SessionsListUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `ShareErrorDismissed returns to SharePreview`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        val bitmap = mockk<Bitmap>()
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.CannotProcessTheImage
        every { shareErrorMapper.map(ShareResult.CannotProcessTheImage) } returns SHARE_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // SharePreview

            viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))
            awaitItem() // Sharing
            awaitItem() // ShareError

            viewModel.onEvent(SessionsListUiEvent.ShareErrorDismissed)

            val content = awaitItem() as SessionsListUiState.Content
            assertTrue(content.overlay is SessionsListOverlay.SharePreview)
        }
    }

    @Test
    fun `ShareClicked with getSession failure shows LoadError`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(RuntimeException())
        coEvery { errorMessageMapper.map(any()) } returns LOAD_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))

            val updatedContent = awaitItem() as SessionsListUiState.Content
            assertTrue(updatedContent.overlay is SessionsListOverlay.LoadError)
            assertEquals(LOAD_ERROR_UI_TEXT, (updatedContent.overlay as SessionsListOverlay.LoadError).message)
        }
    }

    @Test
    fun `ShareClicked with calculateStats failure shows LoadError`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(listOf(session))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { calculateSessionStatsUseCase.calculate(SESSION_ID) } returns Result.failure(RuntimeException())
        coEvery { errorMessageMapper.map(any()) } returns LOAD_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))

            val updatedContent = awaitItem() as SessionsListUiState.Content
            assertTrue(updatedContent.overlay is SessionsListOverlay.LoadError)
            assertEquals(LOAD_ERROR_UI_TEXT, (updatedContent.overlay as SessionsListOverlay.LoadError).message)
        }
    }

    @Test
    fun `LoadErrorDismissed clears overlay`() = runTest {
        val sessions = listOf(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME))
        coEvery { getAllSessionsUseCase.observeAllSessions() } returns flowOf(sessions)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(RuntimeException())
        coEvery { errorMessageMapper.map(any()) } returns LOAD_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionsListUiEvent.ShareClicked(SESSION_ID))
            awaitItem() // LoadError

            viewModel.onEvent(SessionsListUiEvent.LoadErrorDismissed)

            val content = awaitItem() as SessionsListUiState.Content
            assertNull(content.overlay)
        }
    }
}
