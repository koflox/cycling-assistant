package com.koflox.session.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SessionRepositoryImpl(
    private val localDataSource: SessionLocalDataSource,
    private val mapper: SessionMapper,
) : SessionRepository {

    override fun observeActiveSession(): Flow<Session?> {
        return localDataSource.observeFirstSessionByStatuses(
            statuses = listOf(SessionStatus.RUNNING.name, SessionStatus.PAUSED.name),
        ).map { sessionWithTrackPoints ->
            sessionWithTrackPoints?.let { mapper.toDomain(it) }
        }
    }

    override suspend fun saveSession(session: Session): Result<Unit> = suspendRunCatching {
        val sessionEntity = mapper.toEntity(session)
        val trackPointEntities = mapper.toTrackPointEntities(session.id, session.trackPoints)
        localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities)
    }

    override suspend fun getSession(sessionId: String): Result<Session?> = suspendRunCatching {
        localDataSource.getSessionWithTrackPoints(sessionId)?.let {
            mapper.toDomain(it)
        }
    }

}
