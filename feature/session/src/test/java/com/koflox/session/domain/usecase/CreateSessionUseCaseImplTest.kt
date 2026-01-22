package com.koflox.session.domain.usecase

import com.koflox.id.IdGenerator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateSessionUseCaseImplTest {

    companion object {
        private const val GENERATED_ID = "generated-session-id"
        private const val DESTINATION_ID = "dest-456"
        private const val DESTINATION_NAME = "Test Destination"
        private const val DESTINATION_LAT = 52.52
        private const val DESTINATION_LONG = 13.405
        private const val START_LAT = 52.50
        private const val START_LONG = 13.40
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private val idGenerator: IdGenerator = mockk()
    private lateinit var useCase: CreateSessionUseCaseImpl

    @Before
    fun setup() {
        every { idGenerator.generate() } returns GENERATED_ID
        useCase = CreateSessionUseCaseImpl(sessionRepository, idGenerator)
    }

    @Test
    fun `create generates unique session id`() = runTest {
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        useCase.create(createParams())

        coVerify { idGenerator.generate() }
    }

    @Test
    fun `create saves session with generated id`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(GENERATED_ID, sessionSlot.captured.id)
    }

    @Test
    fun `create saves session with destination info`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(DESTINATION_ID, sessionSlot.captured.destinationId)
        assertEquals(DESTINATION_NAME, sessionSlot.captured.destinationName)
        assertEquals(DESTINATION_LAT, sessionSlot.captured.destinationLatitude, 0.0)
        assertEquals(DESTINATION_LONG, sessionSlot.captured.destinationLongitude, 0.0)
    }

    @Test
    fun `create saves session with start location`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(START_LAT, sessionSlot.captured.startLatitude, 0.0)
        assertEquals(START_LONG, sessionSlot.captured.startLongitude, 0.0)
    }

    @Test
    fun `create saves session with running status`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(SessionStatus.RUNNING, sessionSlot.captured.status)
    }

    @Test
    fun `create saves session with zero initial statistics`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(0L, sessionSlot.captured.elapsedTimeMs)
        assertEquals(0.0, sessionSlot.captured.traveledDistanceKm, 0.0)
        assertEquals(0.0, sessionSlot.captured.averageSpeedKmh, 0.0)
        assertEquals(0.0, sessionSlot.captured.topSpeedKmh, 0.0)
    }

    @Test
    fun `create saves session with null end time`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(null, sessionSlot.captured.endTimeMs)
    }

    @Test
    fun `create saves session with initial track point at start location`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(1, sessionSlot.captured.trackPoints.size)
        val trackPoint = sessionSlot.captured.trackPoints[0]
        assertEquals(START_LAT, trackPoint.latitude, 0.0)
        assertEquals(START_LONG, trackPoint.longitude, 0.0)
        assertEquals(0.0, trackPoint.speedKmh, 0.0)
    }

    @Test
    fun `create returns success with session id`() = runTest {
        coEvery { sessionRepository.saveSession(any()) } returns Result.success(Unit)

        val result = useCase.create(createParams())

        assertTrue(result.isSuccess)
        assertEquals(GENERATED_ID, result.getOrNull())
    }

    @Test
    fun `create returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Save failed")
        coEvery { sessionRepository.saveSession(any()) } returns Result.failure(exception)

        val result = useCase.create(createParams())

        assertTrue(result.isFailure)
    }

    @Test
    fun `create sets startTimeMs and lastResumedTimeMs to same value`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        assertEquals(sessionSlot.captured.startTimeMs, sessionSlot.captured.lastResumedTimeMs)
    }

    @Test
    fun `create sets track point timestamp to same as start time`() = runTest {
        val sessionSlot = slot<Session>()
        coEvery { sessionRepository.saveSession(capture(sessionSlot)) } returns Result.success(Unit)

        useCase.create(createParams())

        val trackPoint = sessionSlot.captured.trackPoints[0]
        assertEquals(sessionSlot.captured.startTimeMs, trackPoint.timestampMs)
    }

    private fun createParams() = CreateSessionParams(
        destinationId = DESTINATION_ID,
        destinationName = DESTINATION_NAME,
        destinationLatitude = DESTINATION_LAT,
        destinationLongitude = DESTINATION_LONG,
        startLatitude = START_LAT,
        startLongitude = START_LONG,
    )
}
