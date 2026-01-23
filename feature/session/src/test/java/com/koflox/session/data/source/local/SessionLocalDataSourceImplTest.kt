@file:Suppress("UnusedFlow")

package com.koflox.session.data.source.local

import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.testutil.createSessionEntity
import com.koflox.session.testutil.createSessionWithTrackPoints
import com.koflox.session.testutil.createTrackPointEntity
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionLocalDataSourceImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao: SessionDao = mockk()
    private lateinit var dataSource: SessionLocalDataSourceImpl

    @Before
    fun setup() {
        dataSource = SessionLocalDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            dao = dao,
        )
    }

    @Test
    fun `insertSessionWithTrackPoints delegates to dao`() = runTest {
        val session = createSessionEntity()
        val trackPoints = listOf(createTrackPointEntity())
        coJustRun { dao.insertSessionWithTrackPoints(session, trackPoints) }

        dataSource.insertSessionWithTrackPoints(session, trackPoints)

        coVerify(exactly = 1) { dao.insertSessionWithTrackPoints(session, trackPoints) }
    }

    @Test
    fun `insertSessionWithTrackPoints handles empty track points`() = runTest {
        val session = createSessionEntity()
        coJustRun { dao.insertSessionWithTrackPoints(session, emptyList()) }

        dataSource.insertSessionWithTrackPoints(session, emptyList())

        coVerify { dao.insertSessionWithTrackPoints(session, emptyList()) }
    }

    @Test
    fun `getSessionWithTrackPoints delegates to dao`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        coEvery { dao.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints

        dataSource.getSessionWithTrackPoints(SESSION_ID)

        coVerify(exactly = 1) { dao.getSessionWithTrackPoints(SESSION_ID) }
    }

    @Test
    fun `getSessionWithTrackPoints returns session when found`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()
        coEvery { dao.getSessionWithTrackPoints(SESSION_ID) } returns sessionWithTrackPoints

        val result = dataSource.getSessionWithTrackPoints(SESSION_ID)

        assertEquals(sessionWithTrackPoints, result)
    }

    @Test
    fun `getSessionWithTrackPoints returns null when not found`() = runTest {
        coEvery { dao.getSessionWithTrackPoints(SESSION_ID) } returns null

        val result = dataSource.getSessionWithTrackPoints(SESSION_ID)

        assertNull(result)
    }

    @Test
    fun `observeSessionsByStatuses delegates to dao`() = runTest {
        val statuses = listOf("RUNNING", "PAUSED")
        every { dao.observeSessionsByStatuses(statuses) } returns flowOf(emptyList())

        dataSource.observeSessionsByStatuses(statuses)

        verify(exactly = 1) { dao.observeSessionsByStatuses(statuses) }
    }

    @Test
    fun `observeSessionsByStatuses returns flow from dao`() = runTest {
        val statuses = listOf("RUNNING")
        val sessions = listOf(createTestSessionWithTrackPoints())
        val flow = flowOf(sessions)
        every { dao.observeSessionsByStatuses(statuses) } returns flow

        val result = dataSource.observeSessionsByStatuses(statuses)

        assertEquals(flow, result)
    }

    @Test
    fun `observeFirstSessionByStatuses delegates to dao`() = runTest {
        val statuses = listOf("RUNNING", "PAUSED")
        every { dao.observeFirstSessionByStatuses(statuses) } returns flowOf(null)

        dataSource.observeFirstSessionByStatuses(statuses)

        verify(exactly = 1) { dao.observeFirstSessionByStatuses(statuses) }
    }

    @Test
    fun `observeFirstSessionByStatuses returns flow from dao`() = runTest {
        val statuses = listOf("RUNNING")
        val session = createTestSessionWithTrackPoints()
        val flow = flowOf(session)
        every { dao.observeFirstSessionByStatuses(statuses) } returns flow

        val result = dataSource.observeFirstSessionByStatuses(statuses)

        assertEquals(flow, result)
    }

    @Test
    fun `observeAllSessions delegates to dao`() = runTest {
        every { dao.observeAllSessions() } returns flowOf(emptyList())

        dataSource.observeAllSessions()

        verify(exactly = 1) { dao.observeAllSessions() }
    }

    @Test
    fun `observeAllSessions returns flow from dao`() = runTest {
        val sessions = listOf(createTestSessionWithTrackPoints())
        val flow = flowOf(sessions)
        every { dao.observeAllSessions() } returns flow

        val result = dataSource.observeAllSessions()

        assertEquals(flow, result)
    }

    private fun createTestSessionWithTrackPoints() = createSessionWithTrackPoints(
        session = createSessionEntity(),
        trackPoints = listOf(createTrackPointEntity()),
    )
}
