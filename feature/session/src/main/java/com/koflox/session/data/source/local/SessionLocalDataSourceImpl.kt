package com.koflox.session.data.source.local

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class SessionLocalDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val daoFactory: ConcurrentFactory<SessionDao>,
) : SessionLocalDataSource {

    override suspend fun insertSessionWithTrackPoints(
        session: SessionEntity,
        trackPoints: List<TrackPointEntity>,
    ) = withContext(dispatcherIo) {
        daoFactory.get().insertSessionWithTrackPoints(session, trackPoints)
    }

    override suspend fun getSessionWithTrackPoints(sessionId: String): SessionWithTrackPoints? = withContext(dispatcherIo) {
        daoFactory.get().getSessionWithTrackPoints(sessionId)
    }

    override fun observeFirstSessionByStatuses(statuses: List<String>): Flow<SessionWithTrackPoints?> = flow {
        emitAll(daoFactory.get().observeFirstSessionByStatuses(statuses))
    }

    override fun observeAllSessions(): Flow<List<SessionWithTrackPoints>> = flow {
        emitAll(daoFactory.get().observeAllSessions())
    }
}
