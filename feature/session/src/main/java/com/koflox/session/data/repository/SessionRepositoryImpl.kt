package com.koflox.session.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

internal class SessionRepositoryImpl(
    private val localDataSource: SessionLocalDataSource,
    private val mapper: SessionMapper,
) : SessionRepository {

    private val _hasActiveSession = MutableStateFlow(false)
    override val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    override fun setActiveSession(isActive: Boolean) {
        _hasActiveSession.value = isActive
    }

    override suspend fun saveSession(session: Session): Result<Unit> = suspendRunCatching {
        val sessionEntity = mapper.toEntity(session)
        val trackPointEntities = mapper.toTrackPointEntities(session.id, session.trackPoints)
        localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities)
    }

    override suspend fun getSession(sessionId: String): Result<Session?> = suspendRunCatching {
        val sessionEntity = localDataSource.getSession(sessionId) ?: return@suspendRunCatching null
        val trackPoints = localDataSource.getTrackPoints(sessionId)
        mapper.toDomain(sessionEntity, trackPoints)
    }

    override fun observeCompletedSessions(): Flow<List<Session>> = localDataSource.observeCompletedSessions().map { entities ->
        mapper.toDomainList(entities)
    }
}
