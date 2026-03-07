package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

interface UpdateSessionPowerUseCase {
    suspend fun update(powerWatts: Int, timestampMs: Long)
}

internal class UpdateSessionPowerUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val sessionMutex: Mutex,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
) : UpdateSessionPowerUseCase {

    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000.0
        private val POWER_WINDOW = 10.seconds
        private const val MIN_WINDOW_READINGS = 3
    }

    private var lastReadingTimestampMs: Long? = null
    private val powerWindow = ArrayDeque<TimestampedPower>()

    override suspend fun update(powerWatts: Int, timestampMs: Long) = withContext(dispatcherDefault) {
        sessionMutex.withLock {
            val session = activeSessionUseCase.getActiveSession()
            if (session.status != SessionStatus.RUNNING) return@withLock
            val currentReadings = session.totalPowerReadings ?: 0
            val currentSumWatts = session.sumPowerWatts ?: 0L
            val smoothedPower = medianSmoothedPower(powerWatts, timestampMs)
            val maxPowerWatts = if (smoothedPower != null && session.maxPowerWatts != null) {
                maxOf(session.maxPowerWatts, smoothedPower)
            } else {
                session.maxPowerWatts
            }
            val currentEnergyJoules = session.totalEnergyJoules ?: 0.0
            val energyDelta = calculateEnergyDelta(powerWatts, timestampMs)
            lastReadingTimestampMs = timestampMs
            val updatedSession = session.copy(
                totalPowerReadings = currentReadings + 1,
                sumPowerWatts = currentSumWatts + powerWatts,
                maxPowerWatts = maxPowerWatts,
                totalEnergyJoules = currentEnergyJoules + energyDelta,
            )
            sessionRepository.saveSession(updatedSession)
        }
    }

    private fun medianSmoothedPower(rawPowerWatts: Int, timestampMs: Long): Int? {
        val windowStartMs = timestampMs - POWER_WINDOW.inWholeMilliseconds
        while (powerWindow.isNotEmpty() && powerWindow.first().timestampMs < windowStartMs) {
            powerWindow.removeFirst()
        }
        powerWindow.addLast(TimestampedPower(timestampMs, rawPowerWatts))
        if (powerWindow.size < MIN_WINDOW_READINGS) return null
        val sorted = powerWindow.map(TimestampedPower::powerWatts).sorted()
        return sorted[sorted.size / 2]
    }

    private fun calculateEnergyDelta(powerWatts: Int, timestampMs: Long): Double {
        val lastTs = lastReadingTimestampMs ?: return 0.0
        val deltaSec = (timestampMs - lastTs) / MILLISECONDS_PER_SECOND
        return if (deltaSec <= 0) 0.0 else powerWatts * deltaSec
    }

    private data class TimestampedPower(val timestampMs: Long, val powerWatts: Int)
}
