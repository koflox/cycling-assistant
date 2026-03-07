package com.koflox.session.data.repository

import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.concurrent.suspendRunCatching
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.runtime.FlushInfo
import com.koflox.session.data.source.runtime.SessionRuntimeDataSource
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

internal class SessionRepositoryImpl(
    private val localDataSource: SessionLocalDataSource,
    private val runtimeDataSource: SessionRuntimeDataSource,
    private val mapper: SessionMapper,
    private val flushDecider: SessionFlushDecider,
    private val currentTimeProvider: CurrentTimeProvider,
    private val dispatcherDefault: CoroutineDispatcher,
    private val mutex: Mutex = Mutex(),
) : SessionRepository {

    private val isInitialized = AtomicBoolean(false)

    override fun observeActiveSession(): Flow<Session?> =
        runtimeDataSource.activeSession.onStart {
            if (isInitialized.compareAndSet(false, true)) {
                loadActiveSessionFromDb()
            }
        }.flowOn(dispatcherDefault)

    override fun observeAllSessions(): Flow<List<Session>> =
        localDataSource.observeAllSessions().map { sessions ->
            sessions.map { mapper.toDomain(it) }
        }

    override suspend fun saveSession(session: Session): Result<Unit> = suspendRunCatching {
        mutex.withLock {
            val flushInfo = runtimeDataSource.getFlushInfo()
            if (flushDecider.shouldFlush(session.status, flushInfo.timeMs)) {
                flushToDb(session, flushInfo.trackPointCount)
            }
            if (session.status != SessionStatus.COMPLETED) {
                runtimeDataSource.setActiveSession(session)
            }
        }
    }

    override suspend fun getSession(sessionId: String): Result<Session> = suspendRunCatching {
        mutex.withLock {
            val cached = runtimeDataSource.activeSession.value
            if (cached != null && cached.id == sessionId) return@withLock cached
            val session = localDataSource.getSessionWithTrackPoints(sessionId)
                ?: throw NoSuchElementException("Session not found")
            mapper.toDomain(session)
        }
    }

    private suspend fun flushToDb(session: Session, flushedTrackPointCount: Int) {
        val sessionEntity = mapper.toEntity(session)
        val newTrackPoints = session.trackPoints.drop(flushedTrackPointCount)
        val newTrackPointEntities = mapper.toTrackPointEntities(session.id, newTrackPoints)
        localDataSource.insertSessionWithTrackPoints(sessionEntity, newTrackPointEntities)
        runtimeDataSource.setFlushInfo(
            FlushInfo(
                trackPointCount = session.trackPoints.size,
                timeMs = currentTimeProvider.currentTimeMs(),
            ),
        )
        if (session.status == SessionStatus.COMPLETED) {
            runtimeDataSource.clearActiveSession()
            isInitialized.set(false)
        }
    }

    private suspend fun loadActiveSessionFromDb() {
        mutex.withLock {
            val sessionWithTrackPoints = localDataSource.observeFirstSessionByStatuses(
                listOf(SessionStatus.RUNNING.name, SessionStatus.PAUSED.name),
            ).first() ?: return
            val session = mapper.toDomain(sessionWithTrackPoints)
            runtimeDataSource.setFlushInfo(
                FlushInfo(
                    trackPointCount = session.trackPoints.size,
                    timeMs = currentTimeProvider.currentTimeMs(),
                ),
            )
            runtimeDataSource.setActiveSession(session)
        }
    }
}
