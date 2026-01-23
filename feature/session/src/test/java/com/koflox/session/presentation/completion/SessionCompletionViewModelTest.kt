package com.koflox.session.presentation.completion

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.share.SessionImageSharer
import com.koflox.session.presentation.share.ShareErrorMapper
import com.koflox.session.presentation.share.ShareResult
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionUiModel
import com.koflox.session.testutil.createTrackPoint
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionCompletionViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Destination"
        private const val ERROR_MESSAGE = "Something went wrong"
        private const val SHARE_ERROR_MESSAGE = "Cannot share"
        private const val FORMATTED_DATE = "Jan 1, 2024"
        private const val FORMATTED_TIME = "01:30:00"
        private const val FORMATTED_DISTANCE = "15.5 km"
        private const val FORMATTED_AVG_SPEED = "22.0 km/h"
        private const val FORMATTED_TOP_SPEED = "35.0 km/h"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSessionByIdUseCase: GetSessionByIdUseCase = mockk()
    private val sessionUiMapper: SessionUiMapper = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
    private val imageSharer: SessionImageSharer = mockk()
    private val shareErrorMapper: ShareErrorMapper = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf(SESSION_ID_ARG to SESSION_ID))

    private lateinit var viewModel: SessionCompletionViewModel

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
        coEvery { errorMessageMapper.map(any()) } returns ERROR_MESSAGE
    }

    private fun createViewModel(): SessionCompletionViewModel {
        return SessionCompletionViewModel(
            getSessionByIdUseCase = getSessionByIdUseCase,
            sessionUiMapper = sessionUiMapper,
            errorMessageMapper = errorMessageMapper,
            imageSharer = imageSharer,
            shareErrorMapper = shareErrorMapper,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is SessionCompletionUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadSession success shows Content state`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            assertEquals(SESSION_ID, content.sessionId)
            assertEquals(DESTINATION_NAME, content.destinationName)
            assertEquals(FORMATTED_DATE, content.startDateFormatted)
            assertEquals(FORMATTED_TIME, content.elapsedTimeFormatted)
            assertEquals(FORMATTED_DISTANCE, content.traveledDistanceFormatted)
            assertEquals(FORMATTED_AVG_SPEED, content.averageSpeedFormatted)
            assertEquals(FORMATTED_TOP_SPEED, content.topSpeedFormatted)
            assertNull(content.overlay)
        }
    }

    @Test
    fun `loadSession success maps track points to route points`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41),
                createTrackPoint(latitude = 52.52, longitude = 13.42),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            assertEquals(2, content.routePoints.size)
            assertEquals(52.51, content.routePoints[0].latitude, 0.0)
            assertEquals(13.41, content.routePoints[0].longitude, 0.0)
            assertEquals(52.52, content.routePoints[1].latitude, 0.0)
            assertEquals(13.42, content.routePoints[1].longitude, 0.0)
        }
    }

    @Test
    fun `loadSession failure shows Error state`() = runTest {
        val exception = RuntimeException("Load failed")
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val error = awaitItem() as SessionCompletionUiState.Error
            assertEquals(ERROR_MESSAGE, error.message)
        }
    }

    @Test
    fun `loadSession failure calls error mapper`() = runTest {
        val exception = RuntimeException("Load failed")
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Error
        }

        coVerify { errorMessageMapper.map(exception) }
    }

    @Test
    fun `non-completed session navigates to dashboard`() = runTest {
        val session = createSession(status = SessionStatus.RUNNING)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.navigation.test {
            val navigation = awaitItem()
            assertEquals(SessionCompletionNavigation.ToDashboard, navigation)
        }
    }

    @Test
    fun `non-completed session does not update to Content state`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SessionCompletionUiState.Loading)
            expectNoEvents()
        }
    }

    @Test
    fun `ShareClicked shows share dialog`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareClicked)

            val updatedContent = awaitItem() as SessionCompletionUiState.Content
            assertEquals(Overlay.ShareDialog, updatedContent.overlay)
        }
    }

    @Test
    fun `ShareDialogDismissed clears overlay`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareClicked)
            awaitItem() // ShareDialog

            viewModel.onEvent(SessionCompletionUiEvent.ShareDialogDismissed)

            val content = awaitItem() as SessionCompletionUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `ShareConfirmed shows Sharing state first`() = runTest {
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))

            val sharingState = awaitItem() as SessionCompletionUiState.Content
            assertEquals(Overlay.Sharing, sharingState.overlay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShareConfirmed success shows ShareReady with intent`() = runTest {
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))

            awaitItem() // Sharing
            val readyState = awaitItem() as SessionCompletionUiState.Content
            assertTrue(readyState.overlay is Overlay.ShareReady)
            assertEquals(intent, (readyState.overlay as Overlay.ShareReady).intent)
        }
    }

    @Test
    fun `ShareConfirmed failure shows ShareError`() = runTest {
        val bitmap = mockk<Bitmap>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.CannotProcessTheImage
        every { shareErrorMapper.map(ShareResult.CannotProcessTheImage) } returns SHARE_ERROR_MESSAGE

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))

            awaitItem() // Sharing
            val errorState = awaitItem() as SessionCompletionUiState.Content
            assertTrue(errorState.overlay is Overlay.ShareError)
            assertEquals(SHARE_ERROR_MESSAGE, (errorState.overlay as Overlay.ShareError).message)
        }
    }

    @Test
    fun `ShareConfirmed failure with null error returns to ShareDialog`() = runTest {
        val bitmap = mockk<Bitmap>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.NoAppAvailable
        every { shareErrorMapper.map(ShareResult.NoAppAvailable) } returns null

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))

            awaitItem() // Sharing
            val state = awaitItem() as SessionCompletionUiState.Content
            assertEquals(Overlay.ShareDialog, state.overlay)
        }
    }

    @Test
    fun `ShareIntentLaunched clears overlay`() = runTest {
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))
            awaitItem() // Sharing
            awaitItem() // ShareReady

            viewModel.onEvent(SessionCompletionUiEvent.ShareIntentLaunched)

            val content = awaitItem() as SessionCompletionUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `ErrorDismissed returns to ShareDialog when in ShareError state`() = runTest {
        val bitmap = mockk<Bitmap>()
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))
        coEvery { imageSharer.shareImage(bitmap, DESTINATION_NAME) } returns ShareResult.CannotProcessTheImage
        every { shareErrorMapper.map(ShareResult.CannotProcessTheImage) } returns SHARE_ERROR_MESSAGE

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareConfirmed(bitmap, DESTINATION_NAME))
            awaitItem() // Sharing
            awaitItem() // ShareError

            viewModel.onEvent(SessionCompletionUiEvent.ErrorDismissed)

            val content = awaitItem() as SessionCompletionUiState.Content
            assertEquals(Overlay.ShareDialog, content.overlay)
        }
    }

    @Test
    fun `ErrorDismissed keeps overlay when not in ShareError state`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(SessionCompletionUiEvent.ShareClicked)
            val shareDialogState = awaitItem() as SessionCompletionUiState.Content
            assertEquals(Overlay.ShareDialog, shareDialogState.overlay)

            viewModel.onEvent(SessionCompletionUiEvent.ErrorDismissed)

            // No new emission expected since overlay stays ShareDialog
            expectNoEvents()
            // Verify the state still has ShareDialog
            assertEquals(Overlay.ShareDialog, (viewModel.uiState.value as SessionCompletionUiState.Content).overlay)
        }
    }
}
