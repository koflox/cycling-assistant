package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface UpdateSessionPowerUseCase {
    suspend fun update(powerWatts: Int, timestampMs: Long)
}

internal class UpdateSessionPowerUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val mutex: Mutex,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
) : UpdateSessionPowerUseCase {

    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000.0
    }

    private var lastReadingTimestampMs: Long? = null

    override suspend fun update(powerWatts: Int, timestampMs: Long) = withContext(dispatcherDefault) {
        mutex.withLock {
            val session = activeSessionUseCase.getActiveSession()
            if (session.status != SessionStatus.RUNNING) return@withLock
            val currentReadings = session.totalPowerReadings ?: 0
            val currentSumWatts = session.sumPowerWatts ?: 0L
            val currentMaxWatts = session.maxPowerWatts ?: 0
            val currentEnergyJoules = session.totalEnergyJoules ?: 0.0
            val energyDelta = calculateEnergyDelta(powerWatts, timestampMs)
            lastReadingTimestampMs = timestampMs
            val updatedSession = session.copy(
                totalPowerReadings = currentReadings + 1,
                sumPowerWatts = currentSumWatts + powerWatts,
                maxPowerWatts = maxOf(currentMaxWatts, powerWatts),
                totalEnergyJoules = currentEnergyJoules + energyDelta,
            )
            sessionRepository.saveSession(updatedSession)
        }
    }

    private fun calculateEnergyDelta(powerWatts: Int, timestampMs: Long): Double {
        val lastTs = lastReadingTimestampMs ?: return 0.0
        val deltaSec = (timestampMs - lastTs) / MILLISECONDS_PER_SECOND
        return if (deltaSec <= 0) 0.0 else powerWatts * deltaSec
    }
}
