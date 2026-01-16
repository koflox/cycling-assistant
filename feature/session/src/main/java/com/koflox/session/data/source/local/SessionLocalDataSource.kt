package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

internal interface SessionLocalDataSource {
    suspend fun insertSession(session: SessionEntity)
    suspend fun updateSession(session: SessionEntity)
    suspend fun getSession(sessionId: String): SessionEntity?
    suspend fun deleteSession(sessionId: String)
    fun observeCompletedSessions(): Flow<List<SessionEntity>>
    suspend fun insertTrackPoints(trackPoints: List<TrackPointEntity>)
    suspend fun getTrackPoints(sessionId: String): List<TrackPointEntity>
}
