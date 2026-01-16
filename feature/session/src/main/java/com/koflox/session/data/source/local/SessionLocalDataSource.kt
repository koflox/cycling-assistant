package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

internal interface SessionLocalDataSource {
    suspend fun insertSessionWithTrackPoints(session: SessionEntity, trackPoints: List<TrackPointEntity>)
    suspend fun updateSession(session: SessionEntity)
    suspend fun getSession(sessionId: String): SessionEntity?
    suspend fun deleteSession(sessionId: String)
    fun observeCompletedSessions(): Flow<List<SessionEntity>>
    suspend fun getTrackPoints(sessionId: String): List<TrackPointEntity>
}
