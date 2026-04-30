package com.koflox.sessionstrava.bridge.impl

import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionGpxDataProviderImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
    }

    private val getSessionByIdUseCase: GetSessionByIdUseCase = mockk()

    private val provider = SessionGpxDataProviderImpl(
        getSessionByIdUseCase = getSessionByIdUseCase,
    )

    @Test
    fun `getGpxInput maps session to GpxInput`() = runTest {
        val session = createSession(destinationName = "Mt Fuji", startTimeMs = 1000L)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        val result = provider.getGpxInput(SESSION_ID)

        val gpxInput = result.getOrThrow()
        assertEquals("Mt Fuji", gpxInput.name)
        assertEquals(1000L, gpxInput.startTimeMs)
    }

    @Test
    fun `getGpxInput propagates failure from use case`() = runTest {
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(IllegalStateException("nope"))

        val result = provider.getGpxInput(SESSION_ID)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getGpxInput uses Free Roam when destinationName is null`() = runTest {
        val session = createSession(destinationName = null, startTimeMs = 1000L)
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(session)

        val result = provider.getGpxInput(SESSION_ID)

        assertEquals("Free Roam", result.getOrThrow().name)
    }

    private fun createSession(destinationName: String?, startTimeMs: Long): Session = Session(
        id = SESSION_ID,
        destinationId = null,
        destinationName = destinationName,
        destinationLatitude = null,
        destinationLongitude = null,
        startLatitude = 0.0,
        startLongitude = 0.0,
        startTimeMs = startTimeMs,
        lastResumedTimeMs = startTimeMs,
        endTimeMs = startTimeMs + 1000L,
        elapsedTimeMs = 1000L,
        traveledDistanceKm = 0.0,
        averageSpeedKmh = 0.0,
        topSpeedKmh = 0.0,
        totalAltitudeGainMeters = 0.0,
        status = SessionStatus.COMPLETED,
        trackPoints = emptyList<TrackPoint>(),
        totalPowerReadings = null,
        sumPowerWatts = null,
        maxPowerWatts = null,
        totalEnergyJoules = null,
    )
}
