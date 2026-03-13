package com.koflox.session.presentation.completion

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.usecase.CalculateSessionStatsUseCase
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.navigation.SESSION_ID_ARG
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.route.MapLayer
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionUiModel
import com.koflox.session.testutil.createTrackPoint
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionCompletionViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Destination"
        private val ERROR_UI_TEXT = UiText.Resource(com.koflox.error.R.string.error_not_handled)
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
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase = mockk()
    private val sessionUiMapper: SessionUiMapper = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
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
        coEvery { errorMessageMapper.map(any()) } returns ERROR_UI_TEXT
        every {
            observeStatsDisplayConfigUseCase.observeCompletedSessionStats()
        } returns flowOf(StatsDisplayConfig.DEFAULT_COMPLETED_SESSION_STATS)
        every { sessionUiMapper.buildCompletedSessionStats(any(), any(), any()) } returns emptyList()
    }

    private fun createViewModel(): SessionCompletionViewModel {
        return SessionCompletionViewModel(
            getSessionByIdUseCase = getSessionByIdUseCase,
            calculateSessionStatsUseCase = calculateSessionStatsUseCase,
            observeStatsDisplayConfigUseCase = observeStatsDisplayConfigUseCase,
            sessionUiMapper = sessionUiMapper,
            errorMessageMapper = errorMessageMapper,
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
        }
    }

    @Test
    fun `loadSession success maps track points to route display data`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41, speedKmh = 15.0, isSegmentStart = true),
                createTrackPoint(latitude = 52.52, longitude = 13.42, speedKmh = 20.0),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            val displayData = content.routeDisplayData
            assertEquals(2, displayData.allPoints.size)
            assertEquals(52.51, displayData.allPoints[0].latitude, 0.0)
            assertEquals(13.41, displayData.allPoints[0].longitude, 0.0)
            assertEquals(52.52, displayData.allPoints[1].latitude, 0.0)
            assertEquals(13.42, displayData.allPoints[1].longitude, 0.0)
            assertTrue(displayData.segments.isNotEmpty())
            assertTrue(displayData.gapPolylines.isEmpty())
        }
    }

    @Test
    fun `loadSession splits track points into multiple segments with gap polylines`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41, isSegmentStart = true),
                createTrackPoint(latitude = 52.52, longitude = 13.42),
                createTrackPoint(latitude = 52.53, longitude = 13.43, isSegmentStart = true),
                createTrackPoint(latitude = 52.54, longitude = 13.44),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            val displayData = content.routeDisplayData
            assertEquals(4, displayData.allPoints.size)
            assertEquals(1, displayData.gapPolylines.size)
            assertEquals(52.52, displayData.gapPolylines[0][0].latitude, 0.0)
            assertEquals(52.53, displayData.gapPolylines[0][1].latitude, 0.0)
        }
    }

    @Test
    fun `loadSession filters out segments with fewer than two points but keeps gaps`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41, isSegmentStart = true),
                createTrackPoint(latitude = 52.52, longitude = 13.42),
                createTrackPoint(latitude = 52.53, longitude = 13.43, isSegmentStart = true),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            val displayData = content.routeDisplayData
            assertEquals(1, displayData.segments.size)
            assertEquals(3, displayData.allPoints.size)
            assertEquals(1, displayData.gapPolylines.size)
            assertEquals(52.52, displayData.gapPolylines[0][0].latitude, 0.0)
            assertEquals(52.53, displayData.gapPolylines[0][1].latitude, 0.0)
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
            assertEquals(ERROR_UI_TEXT, error.message)
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
    fun `loadSession initializes with DEFAULT layer`() = runTest {
        coEvery {
            getSessionByIdUseCase.getSession(SESSION_ID)
        } returns Result.success(createSession(id = SESSION_ID, status = SessionStatus.COMPLETED))

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            assertEquals(MapLayer.DEFAULT, content.selectedLayer)
        }
    }

    @Test
    fun `loadSession with power data includes POWER in availableLayers`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            totalPowerReadings = 10,
            sumPowerWatts = 2000L,
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            assertTrue(content.availableLayers.contains(MapLayer.POWER))
            assertEquals(listOf(MapLayer.DEFAULT, MapLayer.SPEED, MapLayer.POWER), content.availableLayers)
        }
    }

    @Test
    fun `loadSession without power data excludes POWER from availableLayers`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as SessionCompletionUiState.Content
            assertEquals(listOf(MapLayer.DEFAULT, MapLayer.SPEED), content.availableLayers)
        }
    }

    @Test
    fun `LayerSelected updates selectedLayer and rebuilds routeDisplayData`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41, speedKmh = 15.0),
                createTrackPoint(latitude = 52.52, longitude = 13.42, speedKmh = 35.0),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val initialContent = awaitItem() as SessionCompletionUiState.Content
            assertEquals(MapLayer.DEFAULT, initialContent.selectedLayer)

            viewModel.onEvent(SessionCompletionUiEvent.LayerSelected(MapLayer.SPEED))

            val updatedContent = awaitItem() as SessionCompletionUiState.Content
            assertEquals(MapLayer.SPEED, updatedContent.selectedLayer)
            assertEquals(initialContent.routeDisplayData.allPoints, updatedContent.routeDisplayData.allPoints)
        }
    }

    @Test
    fun `LayerSelected caches built route data for subsequent switches`() = runTest {
        val session = createSession(
            id = SESSION_ID,
            status = SessionStatus.COMPLETED,
            trackPoints = listOf(
                createTrackPoint(latitude = 52.51, longitude = 13.41, speedKmh = 15.0),
                createTrackPoint(latitude = 52.52, longitude = 13.42, speedKmh = 35.0),
            ),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content - DEFAULT

            viewModel.onEvent(SessionCompletionUiEvent.LayerSelected(MapLayer.SPEED))
            val speedContent = awaitItem() as SessionCompletionUiState.Content
            val speedRouteData = speedContent.routeDisplayData

            viewModel.onEvent(SessionCompletionUiEvent.LayerSelected(MapLayer.DEFAULT))
            awaitItem() // Content - DEFAULT again

            viewModel.onEvent(SessionCompletionUiEvent.LayerSelected(MapLayer.SPEED))
            val cachedSpeedContent = awaitItem() as SessionCompletionUiState.Content
            assertTrue(speedRouteData === cachedSpeedContent.routeDisplayData)
        }
    }
}
