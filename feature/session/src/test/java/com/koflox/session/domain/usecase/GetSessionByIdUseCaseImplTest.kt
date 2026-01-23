package com.koflox.session.domain.usecase

import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetSessionByIdUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: GetSessionByIdUseCaseImpl

    @Before
    fun setup() {
        useCase = GetSessionByIdUseCaseImpl(sessionRepository)
    }

    @Test
    fun `getSession delegates to repository`() = runTest {
        val session = createTestSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        useCase.getSession(SESSION_ID)

        coVerify(exactly = 1) { sessionRepository.getSession(SESSION_ID) }
    }

    @Test
    fun `getSession returns success when session found`() = runTest {
        val session = createTestSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrNull())
    }

    @Test
    fun `getSession returns failure when session not found`() = runTest {
        val exception = NoSuchElementException("Session not found")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(exception)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getSession returns failure on repository error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(exception)

        val result = useCase.getSession(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getSession passes correct session id to repository`() = runTest {
        val customId = "custom-session-id"
        val session = createTestSession(id = customId)
        coEvery { sessionRepository.getSession(customId) } returns Result.success(session)

        useCase.getSession(customId)

        coVerify { sessionRepository.getSession(customId) }
    }

    @Test
    fun `getSession returns session with correct id`() = runTest {
        val session = createTestSession()
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.getSession(SESSION_ID)

        assertEquals(SESSION_ID, result.getOrNull()?.id)
    }

    private fun createTestSession(
        id: String = SESSION_ID,
    ) = createSession(
        id = id,
    )
}
