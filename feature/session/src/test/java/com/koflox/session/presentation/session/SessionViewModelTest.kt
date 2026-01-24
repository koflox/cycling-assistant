package com.koflox.session.presentation.session

import app.cash.turbine.test
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import com.koflox.session.presentation.mapper.SessionUiMapper
import com.koflox.session.presentation.session.timer.SessionTimer
import com.koflox.session.presentation.session.timer.SessionTimerFactory
import com.koflox.session.service.SessionServiceController
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionUiModel
import com.koflox.session.testutil.createTrackPoint
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_ID = "dest-456"
        private const val DESTINATION_NAME = "Test Destination"
        private const val DESTINATION_LATITUDE = 52.52
        private const val DESTINATION_LONGITUDE = 13.405
        private const val START_LATITUDE = 52.50
        private const val START_LONGITUDE = 13.40
        private const val ERROR_MESSAGE = "Something went wrong"
        private const val FORMATTED_TIME = "01:30:00"
        private const val FORMATTED_DISTANCE = "15.5 km"
        private const val FORMATTED_AVG_SPEED = "22.0 km/h"
        private const val FORMATTED_TOP_SPEED = "35.0 km/h"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createSessionUseCase: CreateSessionUseCase = mockk()
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase = mockk()
    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val sessionServiceController: SessionServiceController = mockk(relaxed = true)
    private val sessionUiMapper: SessionUiMapper = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
    private val sessionTimer: SessionTimer = mockk(relaxed = true)
    private val sessionTimerFactory: SessionTimerFactory = mockk()

    private val activeSessionFlow = MutableStateFlow<Session?>(null)

    private lateinit var viewModel: SessionViewModel

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
        every { sessionUiMapper.formatElapsedTime(any()) } returns FORMATTED_TIME
        coEvery { errorMessageMapper.map(any()) } returns ERROR_MESSAGE
        coEvery { activeSessionUseCase.observeActiveSession() } returns activeSessionFlow
        every { sessionTimerFactory.create(any()) } returns sessionTimer
    }

    private fun createViewModel(): SessionViewModel {
        return SessionViewModel(
            createSessionUseCase = createSessionUseCase,
            updateSessionStatusUseCase = updateSessionStatusUseCase,
            activeSessionUseCase = activeSessionUseCase,
            sessionServiceController = sessionServiceController,
            sessionUiMapper = sessionUiMapper,
            errorMessageMapper = errorMessageMapper,
            sessionTimerFactory = sessionTimerFactory,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SessionUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `active session shows Active state`() = runTest {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle

            activeSessionFlow.value = session

            val active = awaitItem() as SessionUiState.Active
            assertEquals(SESSION_ID, active.sessionId)
            assertEquals(DESTINATION_NAME, active.destinationName)
            assertEquals(FORMATTED_TIME, active.elapsedTimeFormatted)
            assertEquals(FORMATTED_DISTANCE, active.traveledDistanceFormatted)
        }
    }

    @Test
    fun `session ending returns to Idle state`() = runTest {
        val session = createSession()

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle

            activeSessionFlow.value = session
            awaitItem() // Active

            activeSessionFlow.value = null
            assertEquals(SessionUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `StopClicked shows StopConfirmation overlay`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.StopClicked)

            val activeWithOverlay = awaitItem() as SessionUiState.Active
            assertEquals(SessionOverlay.StopConfirmation, activeWithOverlay.overlay)
        }
    }

    @Test
    fun `StopConfirmationDismissed clears overlay`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.StopClicked)
            awaitItem() // StopConfirmation

            viewModel.onEvent(SessionUiEvent.StopConfirmationDismissed)

            val active = awaitItem() as SessionUiState.Active
            assertNull(active.overlay)
        }
    }

    @Test
    fun `StopConfirmed calls stop use case and navigates`() = runTest {
        val session = createSession(id = SESSION_ID)
        activeSessionFlow.value = session
        coEvery { updateSessionStatusUseCase.stop() } returns Result.success(Unit)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.StopClicked)
            awaitItem() // StopConfirmation
        }

        viewModel.navigation.test {
            viewModel.onEvent(SessionUiEvent.StopConfirmed)
            val navigation = awaitItem()
            assertEquals(SessionNavigation.ToCompletion(SESSION_ID), navigation)
        }

        coVerify { updateSessionStatusUseCase.stop() }
    }

    @Test
    fun `PauseClicked calls pause use case`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session
        coEvery { updateSessionStatusUseCase.pause() } returns Result.success(Unit)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.PauseClicked)
            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        }

        coVerify { updateSessionStatusUseCase.pause() }
    }

    @Test
    fun `ResumeClicked calls resume use case`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        activeSessionFlow.value = session
        coEvery { updateSessionStatusUseCase.resume() } returns Result.success(Unit)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.ResumeClicked)
            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        }

        coVerify { updateSessionStatusUseCase.resume() }
    }

    @Test
    fun `pause failure shows Error overlay`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session
        val exception = RuntimeException("Pause failed")
        coEvery { updateSessionStatusUseCase.pause() } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.PauseClicked)

            val activeWithError = awaitItem() as SessionUiState.Active
            assertTrue(activeWithError.overlay is SessionOverlay.Error)
            assertEquals(ERROR_MESSAGE, (activeWithError.overlay as SessionOverlay.Error).message)
        }
    }

    @Test
    fun `ErrorDismissed clears overlay`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session
        val exception = RuntimeException("Error")
        coEvery { updateSessionStatusUseCase.pause() } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.PauseClicked)
            awaitItem() // Error

            viewModel.onEvent(SessionUiEvent.ErrorDismissed)

            val active = awaitItem() as SessionUiState.Active
            assertNull(active.overlay)
        }
    }

    @Test
    fun `started session shows notification`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active
        }

        verify { sessionServiceController.startSessionTracking() }
    }

    @Test
    fun `paused session has isPaused true`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertTrue(active.isPaused)
        }
    }

    @Test
    fun `running session has isPaused false`() = runTest {
        val session = createSession(status = SessionStatus.RUNNING)
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertEquals(false, active.isPaused)
        }
    }

    @Test
    fun `startSession creates session and starts tracking`() = runTest {
        coEvery { createSessionUseCase.create(any()) } returns Result.success(SESSION_ID)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.startSession(
                destinationId = DESTINATION_ID,
                destinationName = DESTINATION_NAME,
                destinationLatitude = DESTINATION_LATITUDE,
                destinationLongitude = DESTINATION_LONGITUDE,
                startLatitude = START_LATITUDE,
                startLongitude = START_LONGITUDE,
                startAltitudeMeters = null,
            )
            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        }

        coVerify {
            createSessionUseCase.create(
                match {
                    it.destinationId == DESTINATION_ID &&
                        it.destinationName == DESTINATION_NAME &&
                        it.destinationLatitude == DESTINATION_LATITUDE &&
                        it.destinationLongitude == DESTINATION_LONGITUDE &&
                        it.startLatitude == START_LATITUDE &&
                        it.startLongitude == START_LONGITUDE
                },
            )
        }
        verify { sessionServiceController.startSessionTracking() }
    }

    @Test
    fun `startSession does not create when active session exists`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.startSession(
                destinationId = DESTINATION_ID,
                destinationName = DESTINATION_NAME,
                destinationLatitude = DESTINATION_LATITUDE,
                destinationLongitude = DESTINATION_LONGITUDE,
                startLatitude = START_LATITUDE,
                startLongitude = START_LONGITUDE,
                startAltitudeMeters = null,
            )
        }

        coVerify(exactly = 0) { createSessionUseCase.create(any()) }
    }

    @Test
    fun `startSession failure does not start tracking`() = runTest {
        val exception = RuntimeException("Create failed")
        coEvery { createSessionUseCase.create(any()) } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.startSession(
                destinationId = DESTINATION_ID,
                destinationName = DESTINATION_NAME,
                destinationLatitude = DESTINATION_LATITUDE,
                destinationLongitude = DESTINATION_LONGITUDE,
                startLatitude = START_LATITUDE,
                startLongitude = START_LONGITUDE,
                startAltitudeMeters = null,
            )
        }

        verify(exactly = 0) { sessionServiceController.startSessionTracking() }
    }

    @Test
    fun `resume failure shows Error overlay`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        activeSessionFlow.value = session
        val exception = RuntimeException("Resume failed")
        coEvery { updateSessionStatusUseCase.resume() } returns Result.failure(exception)

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.ResumeClicked)

            val activeWithError = awaitItem() as SessionUiState.Active
            assertTrue(activeWithError.overlay is SessionOverlay.Error)
            assertEquals(ERROR_MESSAGE, (activeWithError.overlay as SessionOverlay.Error).message)
        }
    }

    @Test
    fun `stop failure shows Error overlay`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session
        coEvery { updateSessionStatusUseCase.stop() } returns Result.failure(RuntimeException("Stop failed"))

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.StopClicked)
            awaitItem() // StopConfirmation

            viewModel.onEvent(SessionUiEvent.StopConfirmed)

            val activeWithoutOverlay = awaitItem() as SessionUiState.Active
            assertNull(activeWithoutOverlay.overlay)

            val activeWithError = awaitItem() as SessionUiState.Active
            assertTrue(activeWithError.overlay is SessionOverlay.Error)
        }
    }

    @Test
    fun `active session maps currentLocation from last track point`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(latitude = 52.50, longitude = 13.40),
            createTrackPoint(latitude = 52.51, longitude = 13.41),
            createTrackPoint(latitude = 52.52, longitude = 13.42),
        )
        val session = createSession(trackPoints = trackPoints)
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertEquals(52.52, active.currentLocation?.latitude)
            assertEquals(13.42, active.currentLocation?.longitude)
        }
    }

    @Test
    fun `active session with empty track points has null currentLocation`() = runTest {
        val session = createSession(trackPoints = emptyList())
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertNull(active.currentLocation)
        }
    }

    @Test
    fun `active session maps destination location`() = runTest {
        val session = createSession(
            destinationLatitude = DESTINATION_LATITUDE,
            destinationLongitude = DESTINATION_LONGITUDE,
        )
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertEquals(DESTINATION_LATITUDE, active.destinationLocation.latitude, 0.0)
            assertEquals(DESTINATION_LONGITUDE, active.destinationLocation.longitude, 0.0)
        }
    }

    @Test
    fun `active session maps speed values`() = runTest {
        val session = createSession()
        activeSessionFlow.value = session

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            val active = awaitItem() as SessionUiState.Active
            assertEquals(FORMATTED_AVG_SPEED, active.averageSpeedFormatted)
            assertEquals(FORMATTED_TOP_SPEED, active.topSpeedFormatted)
        }
    }

    @Test
    fun `overlay is preserved when session updates`() = runTest {
        val session = createSession(status = SessionStatus.PAUSED)
        val updatedSession = session.copy(elapsedTimeMs = 6000000L)
        activeSessionFlow.value = session
        coEvery { updateSessionStatusUseCase.pause() } returns Result.failure(RuntimeException("Error"))
        every { sessionUiMapper.toSessionUiModel(updatedSession) } returns createSessionUiModel(
            elapsedTimeFormatted = "02:00:00",
        )

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            awaitItem() // Active

            viewModel.onEvent(SessionUiEvent.PauseClicked)
            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
            val activeWithError = awaitItem() as SessionUiState.Active
            assertTrue(activeWithError.overlay is SessionOverlay.Error)

            // Simulate session update with different elapsed time
            activeSessionFlow.value = updatedSession
            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

            val updatedActive = awaitItem() as SessionUiState.Active
            assertTrue(updatedActive.overlay is SessionOverlay.Error)
            assertEquals("02:00:00", updatedActive.elapsedTimeFormatted)
        }
    }

}
