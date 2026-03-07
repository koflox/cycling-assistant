package com.koflox.session.data.repository

import app.cash.turbine.test
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.runtime.FlushInfo
import com.koflox.session.data.source.runtime.SessionRuntimeDataSource
import com.koflox.session.data.source.runtime.SessionRuntimeDataSourceImpl
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionEntity
import com.koflox.session.testutil.createSessionWithTrackPoints
import com.koflox.session.testutil.createTrackPoint
import com.koflox.session.testutil.createTrackPointEntity
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionRepositoryImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val localDataSource: SessionLocalDataSource = mockk()
    private val mapper: SessionMapper = mockk()
    private val flushDecider: SessionFlushDecider = mockk()
    private lateinit var runtimeDataSource: SessionRuntimeDataSource
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        runtimeDataSource = SessionRuntimeDataSourceImpl()
        every { flushDecider.shouldFlush(any(), any()) } returns false
        repository = SessionRepositoryImpl(
            localDataSource = localDataSource,
            runtimeDataSource = runtimeDataSource,
            mapper = mapper,
            flushDecider = flushDecider,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `observeActiveSession loads from db on first collection`() = runTest {
        every {
            localDataSource.observeFirstSessionByStatuses(
                listOf(SessionStatus.RUNNING.name, SessionStatus.PAUSED.name),
            )
        } returns flowOf(null)

        repository.observeActiveSession().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSession populates runtime cache from db`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        every {
            localDataSource.observeFirstSessionByStatuses(any())
        } returns flowOf(sessionWithTrackPoints)
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        repository.observeActiveSession().test {
            val result = awaitItem()
            assertEquals(domainSession, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSession emits null when no active session in db`() = runTest {
        every { localDataSource.observeFirstSessionByStatuses(any()) } returns flowOf(null)

        repository.observeActiveSession().test {
            val result = awaitItem()
            assertEquals(null, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions delegates to localDataSource`() = runTest {
        every { localDataSource.observeAllSessions() } returns flowOf(emptyList())

        repository.observeAllSessions().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions maps sessions`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        every { localDataSource.observeAllSessions() } returns flowOf(listOf(sessionWithTrackPoints))
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        repository.observeAllSessions().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(domainSession, result[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAllSessions returns empty list when no sessions`() = runTest {
        every { localDataSource.observeAllSessions() } returns flowOf(emptyList())

        repository.observeAllSessions().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveSession updates runtime cache`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)

        repository.saveSession(session)

        assertEquals(session, runtimeDataSource.activeSession.value)
    }

    @Test
    fun `saveSession does not flush to db when flushDecider returns false`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)

        repository.saveSession(session)

        coVerify(exactly = 0) { localDataSource.insertSessionWithTrackPoints(any(), any()) }
    }

    @Test
    fun `saveSession flushes to db when flushDecider returns true`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        val sessionEntity = createSessionEntity()
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(any(), any()) } returns emptyList()
        coJustRun { localDataSource.insertSessionWithTrackPoints(any(), any()) }

        repository.saveSession(session)

        coVerify { localDataSource.insertSessionWithTrackPoints(sessionEntity, any()) }
    }

    @Test
    fun `saveSession passes all track points on first flush`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        val sessionEntity = createSessionEntity()
        val trackPointEntities = listOf(createTrackPointEntity())
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(SESSION_ID, session.trackPoints) } returns trackPointEntities
        coJustRun { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }

        repository.saveSession(session)

        coVerify { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }
    }

    @Test
    fun `saveSession only inserts new track points on subsequent flushes`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(pointIndex = 0),
            createTrackPoint(pointIndex = 1),
            createTrackPoint(pointIndex = 2),
        )
        val session = createTestSession(
            status = SessionStatus.PAUSED,
            trackPoints = trackPoints,
        )
        val sessionEntity = createSessionEntity()
        runtimeDataSource.setFlushInfo(FlushInfo(trackPointCount = 1, timeMs = 0L))
        val newTrackPoints = trackPoints.drop(1)
        val newTrackPointEntities = listOf(createTrackPointEntity(pointIndex = 1), createTrackPointEntity(pointIndex = 2))
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(SESSION_ID, newTrackPoints) } returns newTrackPointEntities
        coJustRun { localDataSource.insertSessionWithTrackPoints(sessionEntity, newTrackPointEntities) }

        repository.saveSession(session)

        coVerify { localDataSource.insertSessionWithTrackPoints(sessionEntity, newTrackPointEntities) }
    }

    @Test
    fun `saveSession updates flush info after flush`() = runTest {
        val trackPoints = listOf(createTrackPoint(pointIndex = 0), createTrackPoint(pointIndex = 1))
        val session = createTestSession(status = SessionStatus.PAUSED, trackPoints = trackPoints)
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns createSessionEntity()
        coEvery { mapper.toTrackPointEntities(any(), any()) } returns emptyList()
        coJustRun { localDataSource.insertSessionWithTrackPoints(any(), any()) }

        repository.saveSession(session)

        assertEquals(2, runtimeDataSource.getFlushInfo().trackPointCount)
    }

    @Test
    fun `saveSession clears runtime on completion`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns createSessionEntity()
        coEvery { mapper.toTrackPointEntities(any(), any()) } returns emptyList()
        coJustRun { localDataSource.insertSessionWithTrackPoints(any(), any()) }

        repository.saveSession(session)

        assertEquals(null, runtimeDataSource.activeSession.value)
        assertEquals(FlushInfo(), runtimeDataSource.getFlushInfo())
    }

    @Test
    fun `saveSession sets cache after flush`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } returns createSessionEntity()
        coEvery { mapper.toTrackPointEntities(any(), any()) } returns emptyList()
        coJustRun { localDataSource.insertSessionWithTrackPoints(any(), any()) }

        repository.saveSession(session)

        assertEquals(session, runtimeDataSource.activeSession.value)
    }

    @Test
    fun `saveSession returns success on successful save`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)

        val result = repository.saveSession(session)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveSession returns failure on error`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        val exception = RuntimeException("Database error")
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } throws exception

        val result = repository.saveSession(session)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `saveSession does not update cache on flush error`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)
        every { flushDecider.shouldFlush(any(), any()) } returns true
        coEvery { mapper.toEntity(session) } throws RuntimeException("Database error")

        repository.saveSession(session)

        assertEquals(null, runtimeDataSource.activeSession.value)
    }

    @Test
    fun `getSession returns cached session when id matches`() = runTest {
        val session = createTestSession(id = SESSION_ID)
        runtimeDataSource.setActiveSession(session)

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrNull())
    }

    @Test
    fun `getSession fetches from db when not in cache`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        assertEquals(domainSession, result.getOrNull())
        coVerify { localDataSource.getSessionWithTrackPoints(SESSION_ID) }
    }

    @Test
    fun `getSession fetches from db when cached session has different id`() = runTest {
        val cachedSession = createTestSession(id = "other-id")
        runtimeDataSource.setActiveSession(cachedSession)
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.getSessionWithTrackPoints(SESSION_ID) }
    }

    @Test
    fun `getSession returns failure when session not found`() = runTest {
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns null

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getSession returns failure on database error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } throws exception

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private fun createTestSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.RUNNING,
        trackPoints: List<TrackPoint> = emptyList(),
    ): Session = createSession(
        id = id,
        status = status,
        trackPoints = trackPoints,
    )

    private fun createTestSessionWithTrackPoints(): SessionWithTrackPoints = createSessionWithTrackPoints(
        session = createSessionEntity(),
        trackPoints = listOf(createTrackPointEntity()),
    )
}
