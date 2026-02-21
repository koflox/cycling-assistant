package com.koflox.session.domain.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface UpdateSessionStatusUseCase {
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun onServiceRestart(): Result<Unit>
}

internal class UpdateSessionStatusUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val mutex: Mutex,
    private val activeSessionUseCase: ActiveSessionUseCase,
    private val sessionRepository: SessionRepository,
) : UpdateSessionStatusUseCase {

    companion object {
        private const val MILLISECONDS_PER_HOUR = 3_600_000.0
    }

    override suspend fun pause(): Result<Unit> = withContext(dispatcherDefault) {
        mutex.withLock {
            suspendRunCatching {
                val session = activeSessionUseCase.getActiveSession()
                if (session.status != SessionStatus.RUNNING) return@suspendRunCatching
                val currentTimeMs = System.currentTimeMillis()
                val elapsedSinceLastResume = currentTimeMs - session.lastResumedTimeMs
                val totalElapsedTimeMs = session.elapsedTimeMs + elapsedSinceLastResume
                val averageSpeedKmh = calculateAverageSpeed(session.traveledDistanceKm, totalElapsedTimeMs)
                val pausedSession = session.copy(
                    status = SessionStatus.PAUSED,
                    elapsedTimeMs = totalElapsedTimeMs,
                    averageSpeedKmh = averageSpeedKmh,
                )
                sessionRepository.saveSession(pausedSession).getOrThrow()
            }
        }
    }

    override suspend fun resume(): Result<Unit> = withContext(dispatcherDefault) {
        mutex.withLock {
            suspendRunCatching {
                val session = activeSessionUseCase.getActiveSession()
                if (session.status != SessionStatus.PAUSED) return@suspendRunCatching
                val currentTimeMs = System.currentTimeMillis()
                val resumedSession = session.copy(
                    status = SessionStatus.RUNNING,
                    lastResumedTimeMs = currentTimeMs,
                )
                sessionRepository.saveSession(resumedSession).getOrThrow()
            }
        }
    }

    override suspend fun stop(): Result<Unit> = withContext(dispatcherDefault) {
        mutex.withLock {
            suspendRunCatching {
                val session = activeSessionUseCase.getActiveSession()
                if (session.status == SessionStatus.COMPLETED) return@suspendRunCatching
                val currentTimeMs = System.currentTimeMillis()
                val finalElapsedTimeMs = if (session.status == SessionStatus.RUNNING) {
                    session.elapsedTimeMs + (currentTimeMs - session.lastResumedTimeMs)
                } else {
                    session.elapsedTimeMs
                }
                val averageSpeedKmh = calculateAverageSpeed(session.traveledDistanceKm, finalElapsedTimeMs)
                val completedSession = session.copy(
                    status = SessionStatus.COMPLETED,
                    endTimeMs = currentTimeMs,
                    elapsedTimeMs = finalElapsedTimeMs,
                    averageSpeedKmh = averageSpeedKmh,
                    topSpeedKmh = maxOf(session.topSpeedKmh, averageSpeedKmh),
                )
                sessionRepository.saveSession(completedSession).getOrThrow()
            }
        }
    }

    override suspend fun onServiceRestart(): Result<Unit> = withContext(dispatcherDefault) {
        mutex.withLock {
            suspendRunCatching {
                val session = activeSessionUseCase.getActiveSession()
                if (session.status != SessionStatus.RUNNING) return@suspendRunCatching
                val currentTimeMs = System.currentTimeMillis()
                val updatedSession = session.copy(
                    lastResumedTimeMs = currentTimeMs,
                )
                sessionRepository.saveSession(updatedSession).getOrThrow()
            }
        }
    }

    private fun calculateAverageSpeed(distanceKm: Double, elapsedTimeMs: Long): Double =
        if (elapsedTimeMs > 0) (distanceKm / elapsedTimeMs) * MILLISECONDS_PER_HOUR else 0.0
}
