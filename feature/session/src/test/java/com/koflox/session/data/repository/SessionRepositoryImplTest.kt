package com.koflox.session.data.repository

import app.cash.turbine.test
import com.koflox.session.data.mapper.SessionMapper
import com.koflox.session.data.source.local.SessionLocalDataSource
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionEntity
import com.koflox.session.testutil.createSessionWithTrackPoints
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
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        repository = SessionRepositoryImpl(
            localDataSource = localDataSource,
            mapper = mapper,
        )
    }

    @Test
    fun `observeActiveSession queries for running and paused statuses`() = runTest {
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
    fun `observeActiveSession maps session when found`() = runTest {
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
    fun `observeActiveSession returns null when no active session`() = runTest {
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
    fun `saveSession maps session to entity`() = runTest {
        val session = createTestSession()
        val sessionEntity = createSessionEntity()
        val trackPointEntities = listOf(createTrackPointEntity())
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(SESSION_ID, session.trackPoints) } returns trackPointEntities
        coJustRun { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }

        repository.saveSession(session)

        coVerify { mapper.toEntity(session) }
    }

    @Test
    fun `saveSession maps track points to entities`() = runTest {
        val session = createTestSession()
        val sessionEntity = createSessionEntity()
        val trackPointEntities = listOf(createTrackPointEntity())
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(SESSION_ID, session.trackPoints) } returns trackPointEntities
        coJustRun { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }

        repository.saveSession(session)

        coVerify { mapper.toTrackPointEntities(SESSION_ID, session.trackPoints) }
    }

    @Test
    fun `saveSession inserts session with track points`() = runTest {
        val session = createTestSession()
        val sessionEntity = createSessionEntity()
        val trackPointEntities = listOf(createTrackPointEntity())
        coEvery { mapper.toEntity(session) } returns sessionEntity
        coEvery { mapper.toTrackPointEntities(SESSION_ID, session.trackPoints) } returns trackPointEntities
        coJustRun { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }

        repository.saveSession(session)

        coVerify { localDataSource.insertSessionWithTrackPoints(sessionEntity, trackPointEntities) }
    }

    @Test
    fun `saveSession returns success on successful save`() = runTest {
        val session = createTestSession()
        coEvery { mapper.toEntity(session) } returns createSessionEntity()
        coEvery { mapper.toTrackPointEntities(any(), any()) } returns emptyList()
        coJustRun { localDataSource.insertSessionWithTrackPoints(any(), any()) }

        val result = repository.saveSession(session)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveSession returns failure on error`() = runTest {
        val session = createTestSession()
        val exception = RuntimeException("Database error")
        coEvery { mapper.toEntity(session) } throws exception

        val result = repository.saveSession(session)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getSession fetches session from localDataSource`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        repository.getSession(SESSION_ID)

        coVerify { localDataSource.getSessionWithTrackPoints(SESSION_ID) }
    }

    @Test
    fun `getSession maps session to domain`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        repository.getSession(SESSION_ID)

        coVerify { mapper.toDomain(sessionWithTrackPoints) }
    }

    @Test
    fun `getSession returns success with session when found`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        val domainSession = createTestSession()
        coEvery { localDataSource.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints
        coEvery { mapper.toDomain(sessionWithTrackPoints) } returns domainSession

        val result = repository.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        assertEquals(domainSession, result.getOrNull())
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
    ): Session = createSession(
        id = id,
    )

    private fun createTestSessionWithTrackPoints(): SessionWithTrackPoints = createSessionWithTrackPoints(
        session = createSessionEntity(),
        trackPoints = listOf(createTrackPointEntity()),
    )
}
