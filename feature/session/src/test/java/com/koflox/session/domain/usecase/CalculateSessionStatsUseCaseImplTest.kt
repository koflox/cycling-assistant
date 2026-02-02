package com.koflox.session.domain.usecase

import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createTrackPoint
import com.koflox.sessionsettings.bridge.api.RiderProfileUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateSessionStatsUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val RIDER_WEIGHT_KG = 75f
        private const val ELAPSED_TIME_MS = 3_600_000L
        private const val AVERAGE_SPEED_KMH = 20.0
    }

    private val getSessionByIdUseCase: GetSessionByIdUseCase = mockk()
    private val riderProfileUseCase: RiderProfileUseCase = mockk()

    private lateinit var useCase: CalculateSessionStatsUseCaseImpl

    @Before
    fun setup() {
        coEvery { riderProfileUseCase.getRiderWeightKg() } returns RIDER_WEIGHT_KG
        useCase = CalculateSessionStatsUseCaseImpl(
            getSessionByIdUseCase = getSessionByIdUseCase,
            riderProfileUseCase = riderProfileUseCase,
        )
    }

    @Test
    fun `moving time covers all gaps when speed is above threshold`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 15.0),
            createTrackPoint(timestampMs = 3000L, speedKmh = 20.0),
            createTrackPoint(timestampMs = 6000L, speedKmh = 18.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 6000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(6000L, result.movingTimeMs)
        assertEquals(0L, result.idleTimeMs)
    }

    @Test
    fun `idle time accumulates when speed is below threshold`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 15.0),
            createTrackPoint(timestampMs = 3000L, speedKmh = 1.0),
            createTrackPoint(timestampMs = 6000L, speedKmh = 0.5),
            createTrackPoint(timestampMs = 9000L, speedKmh = 20.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 9000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(3000L, result.movingTimeMs)
        assertEquals(6000L, result.idleTimeMs)
    }

    @Test
    fun `moving time is sum of gaps with speed above threshold`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 15.0),
            createTrackPoint(timestampMs = 3000L, speedKmh = 1.0),
            createTrackPoint(timestampMs = 6000L, speedKmh = 20.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 6000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(3000L, result.movingTimeMs)
        assertEquals(3000L, result.idleTimeMs)
    }

    @Test
    fun `time not covered by track points is treated as idle`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 15.0),
            createTrackPoint(timestampMs = 3000L, speedKmh = 20.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 10_000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(3000L, result.movingTimeMs)
        assertEquals(7000L, result.idleTimeMs)
    }

    @Test
    fun `moving time does not exceed elapsed time`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 15.0),
            createTrackPoint(timestampMs = 5000L, speedKmh = 20.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 3000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(3000L, result.movingTimeMs)
        assertEquals(0L, result.idleTimeMs)
    }

    @Test
    fun `idle time does not go below zero`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 0.0),
            createTrackPoint(timestampMs = 5000L, speedKmh = 0.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = 1000L, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0L, result.movingTimeMs)
        assertEquals(1000L, result.idleTimeMs)
    }

    @Test
    fun `altitude loss accumulates decreases above threshold`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(altitudeMeters = 100.0),
            createTrackPoint(altitudeMeters = 95.0),
            createTrackPoint(altitudeMeters = 90.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(10.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `altitude loss ignores decreases at or below threshold`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(altitudeMeters = 100.0),
            createTrackPoint(altitudeMeters = 99.5),
            createTrackPoint(altitudeMeters = 99.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `altitude loss ignores increases`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(altitudeMeters = 100.0),
            createTrackPoint(altitudeMeters = 110.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `altitude loss handles null altitude`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(altitudeMeters = 100.0),
            createTrackPoint(altitudeMeters = null),
            createTrackPoint(altitudeMeters = 80.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `calories uses correct MET for low speed`() = runTest {
        val movingTimeHours = 1.0
        val movingTimeMs = (movingTimeHours * 3_600_000).toLong()
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 10.0),
            createTrackPoint(timestampMs = movingTimeMs, speedKmh = 10.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = movingTimeMs, averageSpeedKmh = 10.0),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(4.0 * RIDER_WEIGHT_KG * movingTimeHours, result.caloriesBurned!!, 0.01)
    }

    @Test
    fun `calories uses correct MET for moderate speed`() = runTest {
        val movingTimeHours = 1.0
        val movingTimeMs = (movingTimeHours * 3_600_000).toLong()
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 17.0),
            createTrackPoint(timestampMs = movingTimeMs, speedKmh = 17.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = movingTimeMs, averageSpeedKmh = 17.0),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(6.8 * RIDER_WEIGHT_KG * movingTimeHours, result.caloriesBurned!!, 0.01)
    }

    @Test
    fun `calories uses correct MET for high speed`() = runTest {
        val movingTimeHours = 1.0
        val movingTimeMs = (movingTimeHours * 3_600_000).toLong()
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 30.0),
            createTrackPoint(timestampMs = movingTimeMs, speedKmh = 30.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = movingTimeMs, averageSpeedKmh = 30.0),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(12.0 * RIDER_WEIGHT_KG * movingTimeHours, result.caloriesBurned!!, 0.01)
    }

    @Test
    fun `calories is null when rider weight is not set`() = runTest {
        coEvery { riderProfileUseCase.getRiderWeightKg() } returns null
        val movingTimeMs = 3_600_000L
        val trackPoints = listOf(
            createTrackPoint(timestampMs = 0L, speedKmh = 10.0),
            createTrackPoint(timestampMs = movingTimeMs, speedKmh = 10.0),
        )
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = movingTimeMs, averageSpeedKmh = 10.0),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertNull(result.caloriesBurned)
    }

    @Test
    fun `empty track points treats all time as idle`() = runTest {
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0L, result.movingTimeMs)
        assertEquals(ELAPSED_TIME_MS, result.idleTimeMs)
        assertEquals(0.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `single track point treats all time as idle`() = runTest {
        val trackPoints = listOf(createTrackPoint(timestampMs = 0L, speedKmh = 0.0, altitudeMeters = 100.0))
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.success(
            createSession(trackPoints = trackPoints, elapsedTimeMs = ELAPSED_TIME_MS, averageSpeedKmh = AVERAGE_SPEED_KMH),
        )

        val result = useCase.calculate(SESSION_ID).getOrThrow()

        assertEquals(0L, result.movingTimeMs)
        assertEquals(ELAPSED_TIME_MS, result.idleTimeMs)
        assertEquals(0.0, result.altitudeLossMeters, 0.01)
    }

    @Test
    fun `returns failure when session not found`() = runTest {
        val exception = RuntimeException("Not found")
        coEvery { getSessionByIdUseCase.getSession(SESSION_ID) } returns Result.failure(exception)

        val result = useCase.calculate(SESSION_ID)

        assertTrue(result.isFailure)
    }
}
