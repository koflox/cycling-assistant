package com.koflox.session.presentation.share

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.koflox.designsystem.text.UiText
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionUiModel
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShareViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Destination"
        private const val SHARE_TEXT = "Check out my ride!"
        private const val CHOOSER_TITLE = "Share via"
        private val SHARE_ERROR_UI_TEXT = UiText.Resource(com.koflox.session.R.string.share_image_processing_error)
        private val GPX_ERROR_UI_TEXT = UiText.Resource(com.koflox.session.R.string.gpx_export_file_error)
        private const val FORMATTED_DATE = "Jan 1, 2024"
        private const val FORMATTED_TIME = "01:30:00"
        private const val FORMATTED_DISTANCE = "15.5 km"
        private const val FORMATTED_AVG_SPEED = "22.0 km/h"
        private const val FORMATTED_TOP_SPEED = "35.0 km/h"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSessionByIdUseCase: GetSessionByIdUseCase = mockk()
    private val calculateSessionStatsUseCase: CalculateSessionStatsUseCase = mockk()
    private val sessionUiMapper: SessionUiMapper = mockk()
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase = mockk()
    private val imageSharer: SessionImageSharer = mockk()
    private val shareErrorMapper: ShareErrorMapper = mockk()
    private val gpxMapper: GpxMapper = mockk()
    private val gpxSharer: SessionGpxSharer = mockk()
    private val gpxShareErrorMapper: GpxShareErrorMapper = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf(SESSION_ID_ARG to SESSION_ID))

    private lateinit var viewModel: ShareViewModel

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
        every { sessionUiMapper.formatPower(any()) } returns "0 W"
        coEvery { calculateSessionStatsUseCase.calculate(any()) } returns Result.success(
            SessionDerivedStats(
                idleTimeMs = 0L,
                movingTimeMs = 0L,
                altitudeLossMeters = 0.0,
                caloriesBurned = 0.0,
            ),
        )
        every {
            observeStatsDisplayConfigUseCase.observeShareStats()
        } returns flowOf(StatsDisplayConfig.DEFAULT_SHARE_STATS)
        every { sessionUiMapper.buildCompletedSessionStats(any(), any(), any()) } returns emptyList()
        every { gpxSharer.isGpxSharingAvailable() } returns true
    }

    private fun createViewModel(): ShareViewModel {
        return ShareViewModel(
            savedStateHandle = savedStateHandle,
            getSessionByIdUseCase = getSessionByIdUseCase,
            calculateSessionStatsUseCase = calculateSessionStatsUseCase,
            sessionUiMapper = sessionUiMapper,
            observeStatsDisplayConfigUseCase = observeStatsDisplayConfigUseCase,
            imageSharer = imageSharer,
            shareErrorMapper = shareErrorMapper,
            gpxMapper = gpxMapper,
            gpxSharer = gpxSharer,
            gpxShareErrorMapper = gpxShareErrorMapper,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is ShareUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadSession success shows Content with Idle gpx state when available`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        every { gpxSharer.isGpxSharingAvailable() } returns true

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as ShareUiState.Content
            assertEquals(SESSION_ID, content.sharePreviewData.sessionId)
            assertEquals(DESTINATION_NAME, content.sharePreviewData.destinationName)
            assertTrue(content.gpxShareState is GpxShareState.Idle)
            assertEquals(ShareTab.IMAGE, content.selectedTab)
        }
    }

    @Test
    fun `loadSession success shows Unavailable gpx state when no app`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        every { gpxSharer.isGpxSharingAvailable() } returns false

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as ShareUiState.Content
            assertTrue(content.gpxShareState is GpxShareState.Unavailable)
        }
    }

    @Test
    fun `TabSelected updates selectedTab`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content tab 0

            viewModel.onEvent(ShareUiEvent.TabSelected(ShareTab.GPX))

            val updated = awaitItem() as ShareUiState.Content
            assertEquals(ShareTab.GPX, updated.selectedTab)
        }
    }

    @Test
    fun `Image ShareConfirmed success shows Ready`() = runTest {
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(ShareUiEvent.Image.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))

            val sharingState = awaitItem() as ShareUiState.Content
            assertTrue(sharingState.imageShareState is ImageShareState.Sharing)

            val readyState = awaitItem() as ShareUiState.Content
            assertTrue(readyState.imageShareState is ImageShareState.Ready)
            assertEquals(intent, (readyState.imageShareState as ImageShareState.Ready).intent)
        }
    }

    @Test
    fun `Image ShareConfirmed failure shows Error`() = runTest {
        val bitmap = mockk<Bitmap>()
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.CannotProcessTheImage
        every { shareErrorMapper.map(ShareResult.CannotProcessTheImage) } returns SHARE_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(ShareUiEvent.Image.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))

            awaitItem() // Sharing
            val errorState = awaitItem() as ShareUiState.Content
            assertTrue(errorState.imageShareState is ImageShareState.Error)
            assertEquals(SHARE_ERROR_UI_TEXT, (errorState.imageShareState as ImageShareState.Error).message)
        }
    }

    @Test
    fun `Image IntentLaunched resets to Idle`() = runTest {
        val bitmap = mockk<Bitmap>()
        val intent = mockk<Intent>()
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { imageSharer.shareImage(bitmap, SHARE_TEXT, CHOOSER_TITLE) } returns ShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(ShareUiEvent.Image.ShareConfirmed(bitmap, SHARE_TEXT, CHOOSER_TITLE))
            awaitItem() // Sharing
            awaitItem() // Ready

            viewModel.onEvent(ShareUiEvent.Image.IntentLaunched)

            val content = awaitItem() as ShareUiState.Content
            assertTrue(content.imageShareState is ImageShareState.Idle)
        }
    }

    @Test
    fun `Gpx ShareClicked success shows Ready`() = runTest {
        val intent = mockk<Intent>()
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        every { gpxMapper.map(session) } returns "<gpx/>"
        coEvery { gpxSharer.shareGpx("<gpx/>", "session_export", DESTINATION_NAME) } returns GpxShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(ShareUiEvent.Gpx.ShareClicked)

            val generatingState = awaitItem() as ShareUiState.Content
            assertTrue(generatingState.gpxShareState is GpxShareState.Generating)

            val readyState = awaitItem() as ShareUiState.Content
            assertTrue(readyState.gpxShareState is GpxShareState.Ready)
            assertEquals(intent, (readyState.gpxShareState as GpxShareState.Ready).intent)
        }
    }

    @Test
    fun `Gpx ShareClicked failure shows Error`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        every { gpxMapper.map(session) } returns "<gpx/>"
        coEvery { gpxSharer.shareGpx("<gpx/>", "session_export", DESTINATION_NAME) } returns GpxShareResult.CannotWriteFile
        every { gpxShareErrorMapper.map(GpxShareResult.CannotWriteFile) } returns GPX_ERROR_UI_TEXT

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content

            viewModel.onEvent(ShareUiEvent.Gpx.ShareClicked)

            awaitItem() // Generating
            val errorState = awaitItem() as ShareUiState.Content
            assertTrue(errorState.gpxShareState is GpxShareState.Error)
            assertEquals(GPX_ERROR_UI_TEXT, (errorState.gpxShareState as GpxShareState.Error).message)
        }
    }

    @Test
    fun `Gpx IntentLaunched resets to Idle`() = runTest {
        val intent = mockk<Intent>()
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)
        every { gpxMapper.map(session) } returns "<gpx/>"
        coEvery { gpxSharer.shareGpx("<gpx/>", "session_export", DESTINATION_NAME) } returns GpxShareResult.Success(intent)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(ShareUiEvent.Gpx.ShareClicked)
            awaitItem() // Generating
            awaitItem() // Ready

            viewModel.onEvent(ShareUiEvent.Gpx.IntentLaunched)

            val content = awaitItem() as ShareUiState.Content
            assertTrue(content.gpxShareState is GpxShareState.Idle)
        }
    }
}
