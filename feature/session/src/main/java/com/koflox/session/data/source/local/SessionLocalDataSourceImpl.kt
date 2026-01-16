package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.data.source.local.entity.SessionEntity
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

    override suspend fun updateSession(session: SessionEntity) = withContext(dispatcherIo) {
        dao.updateSession(session)
    }

    override suspend fun getSession(sessionId: String): SessionEntity? = withContext(dispatcherIo) {
        dao.getSession(sessionId)
    }

    override suspend fun deleteSession(sessionId: String) = withContext(dispatcherIo) {
        dao.deleteSession(sessionId)
    }

    override fun observeCompletedSessions(): Flow<List<SessionEntity>> = dao.observeCompletedSessions()

    override suspend fun getTrackPoints(sessionId: String): List<TrackPointEntity> = withContext(dispatcherIo) {
        dao.getTrackPoints(sessionId)
    }
}
