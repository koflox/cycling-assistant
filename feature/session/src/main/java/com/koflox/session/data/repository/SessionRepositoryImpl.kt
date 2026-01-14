package com.koflox.session.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SessionRepositoryImpl(
    private val dispatcherIo: CoroutineDispatcher,
    // TODO: create DS layer abstraction
    private val sessionDao: SessionDao,
    private val mapper: SessionMapper,
) : SessionRepository {

    private val _hasActiveSession = MutableStateFlow(false)
    override val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    override fun setActiveSession(isActive: Boolean) {
        _hasActiveSession.value = isActive
    }

    override suspend fun saveSession(session: Session): Result<Unit> = withContext(dispatcherIo) {
        suspendRunCatching {
            val sessionEntity = mapper.toEntity(session)
            val trackPointEntities = mapper.toTrackPointEntities(session.id, session.trackPoints)

            // TODO: insert via transaction
            sessionDao.insertSession(sessionEntity)
            if (trackPointEntities.isNotEmpty()) {
                sessionDao.insertTrackPoints(trackPointEntities)
            }
        }
    }

    override suspend fun getSession(sessionId: String): Result<Session?> = withContext(dispatcherIo) {
        suspendRunCatching {
            val sessionEntity = sessionDao.getSession(sessionId) ?: return@suspendRunCatching null
            val trackPoints = sessionDao.getTrackPoints(sessionId)
            mapper.toDomain(sessionEntity, trackPoints)
        }
    }

    override fun observeCompletedSessions(): Flow<List<Session>> = sessionDao.observeCompletedSessions().map { entities ->
        mapper.toDomainList(entities)
    }

}
