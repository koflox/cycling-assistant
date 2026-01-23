package com.koflox.session.domain.usecase

import app.cash.turbine.test
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActiveSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: ActiveSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = ActiveSessionUseCaseImpl(sessionRepository)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeActiveSession delegates to repository`() = runTest {
        val flow = flowOf<Session?>(null)
        every { sessionRepository.observeActiveSession() } returns flow

        useCase.observeActiveSession()

        verify(exactly = 1) { sessionRepository.observeActiveSession() }
    }

    @Test
    fun `observeActiveSession returns session when active`() = runTest {
        val session = createTestSession()
        every { sessionRepository.observeActiveSession() } returns flowOf(session)

        useCase.observeActiveSession().test {
            val result = awaitItem()
            assertEquals(session, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeActiveSession returns null when no active session`() = runTest {
        every { sessionRepository.observeActiveSession() } returns flowOf(null)

        useCase.observeActiveSession().test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasActiveSession returns true when session exists`() = runTest {
        val session = createTestSession()
        every { sessionRepository.observeActiveSession() } returns flowOf(session)

        useCase.hasActiveSession().test {
            val result = awaitItem()
            assertTrue(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasActiveSession returns false when no session`() = runTest {
        every { sessionRepository.observeActiveSession() } returns flowOf(null)

        useCase.hasActiveSession().test {
            val result = awaitItem()
            assertFalse(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasActiveSession emits updates when session changes`() = runTest {
        val session = createTestSession()
        every { sessionRepository.observeActiveSession() } returns flowOf(null, session, null)

        useCase.hasActiveSession().test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveSession returns session when exists`() = runTest {
        val session = createTestSession()
        every { sessionRepository.observeActiveSession() } returns flowOf(session)

        val result = useCase.getActiveSession()

        assertEquals(session, result)
    }

    @Test
    fun `getActiveSession throws NoActiveSessionException when no session`() = runTest {
        every { sessionRepository.observeActiveSession() } returns flowOf(null)

        try {
            useCase.getActiveSession()
            assertTrue("Expected NoActiveSessionException", false)
        } catch (e: NoActiveSessionException) {
            // Reaching this block means the expected exception was thrown - test passes
            assertTrue(true)
        }
    }

    @Test
    fun `getActiveSession returns first emission from flow`() = runTest {
        val session1 = createTestSession(id = "session-1")
        val session2 = createTestSession(id = "session-2")
        every { sessionRepository.observeActiveSession() } returns flowOf(session1, session2)

        val result = useCase.getActiveSession()

        assertEquals("session-1", result.id)
    }

    private fun createTestSession(
        id: String = SESSION_ID,
    ) = createSession(
        id = id,
    )
}
