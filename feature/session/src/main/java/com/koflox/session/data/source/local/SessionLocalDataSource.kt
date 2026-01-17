package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

internal interface SessionLocalDataSource {
    suspend fun insertSessionWithTrackPoints(session: SessionEntity, trackPoints: List<TrackPointEntity>)
    suspend fun getSessionWithTrackPoints(sessionId: String): SessionWithTrackPoints?
    fun observeSessionsByStatuses(statuses: List<String>): Flow<List<SessionWithTrackPoints>>
    fun observeFirstSessionByStatuses(statuses: List<String>): Flow<SessionWithTrackPoints?>
}
