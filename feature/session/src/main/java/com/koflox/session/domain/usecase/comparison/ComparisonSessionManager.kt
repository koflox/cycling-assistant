package com.koflox.session.domain.usecase.comparison

import com.koflox.location.model.Location
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus

/**
 * Coordinates shadow sessions for location processing comparison.
 *
 * When the main session receives location updates, [onLocationUpdate] forwards them
 * to all [ComparisonLocationProcessor] variants. Each processor applies its own
 * historical algorithm and maintains a separate in-memory [Session].
 *
 * On main session completion, [onSessionCompleted] flushes all shadow sessions
 * directly to the local database so they appear in the session list for comparison.
 */
internal interface ComparisonSessionManager {
    suspend fun onLocationUpdate(location: Location, timestampMs: Long, activeSession: Session)
    suspend fun onSessionCompleted(activeSession: Session)
    fun reset()
}

internal class ComparisonSessionManagerImpl(
    private val processors: List<ComparisonLocationProcessor>,
    private val localDataSource: SessionLocalDataSource,
    private val mapper: SessionMapper,
) : ComparisonSessionManager {

    private var isInitialized = false

    override suspend fun onLocationUpdate(location: Location, timestampMs: Long, activeSession: Session) {
        if (!isInitialized) {
            processors.forEach { it.initialize(activeSession) }
            isInitialized = true
        }
        processors.forEach { processor ->
            processor.update(location, timestampMs, activeSession.lastResumedTimeMs)
        }
    }

    override suspend fun onSessionCompleted(activeSession: Session) {
        if (!isInitialized) return
        processors.forEach { processor ->
            val session = processor.getSession() ?: return@forEach
            val completedSession = session.copy(
                status = SessionStatus.COMPLETED,
                endTimeMs = activeSession.endTimeMs,
            )
            val entity = mapper.toEntity(completedSession)
            val trackPointEntities = mapper.toTrackPointEntities(
                completedSession.id, completedSession.trackPoints,
            )
            localDataSource.insertSessionWithTrackPoints(entity, trackPointEntities)
        }
        reset()
    }

    override fun reset() {
        processors.forEach { it.reset() }
        isInitialized = false
    }
}
