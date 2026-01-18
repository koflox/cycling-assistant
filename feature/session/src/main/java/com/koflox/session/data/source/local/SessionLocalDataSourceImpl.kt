package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class SessionLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val dao: SessionDao,
) : SessionLocalDataSource {

    override suspend fun insertSessionWithTrackPoints(
        session: SessionEntity,
        trackPoints: List<TrackPointEntity>,
    ) = withContext(dispatcherIo) {
        dao.insertSessionWithTrackPoints(session, trackPoints)
    }

    override suspend fun getSessionWithTrackPoints(sessionId: String): SessionWithTrackPoints? = withContext(dispatcherIo) {
        dao.getSessionWithTrackPoints(sessionId)
    }

    override fun observeSessionsByStatuses(statuses: List<String>): Flow<List<SessionWithTrackPoints>> = dao.observeSessionsByStatuses(statuses)

    override fun observeFirstSessionByStatuses(statuses: List<String>): Flow<SessionWithTrackPoints?> = dao.observeFirstSessionByStatuses(statuses)

    override fun observeAllSessions(): Flow<List<SessionWithTrackPoints>> = dao.observeAllSessions()
}
