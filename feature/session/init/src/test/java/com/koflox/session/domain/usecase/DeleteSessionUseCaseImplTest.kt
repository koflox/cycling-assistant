package com.koflox.session.domain.usecase

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.repository.SessionRepository
import com.koflox.session.testutil.createSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
    }

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: DeleteSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = DeleteSessionUseCaseImpl(sessionRepository)
    }

    @Test
    fun `delete completed session delegates to repository`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { sessionRepository.deleteSession(SESSION_ID) } returns Result.success(Unit)

        val result = useCase.delete(SESSION_ID)

        assertTrue(result.isSuccess)
        coVerify { sessionRepository.deleteSession(SESSION_ID) }
    }

    @Test
    fun `delete running session fails with SessionNotDeletableException`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.RUNNING)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.delete(SESSION_ID)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SessionNotDeletableException)
        assertEquals(SessionStatus.RUNNING, (error as SessionNotDeletableException).status)
        coVerify(exactly = 0) { sessionRepository.deleteSession(any()) }
    }

    @Test
    fun `delete paused session fails with SessionNotDeletableException`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.PAUSED)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.delete(SESSION_ID)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SessionNotDeletableException)
        coVerify(exactly = 0) { sessionRepository.deleteSession(any()) }
    }

    @Test
    fun `delete propagates getSession failure`() = runTest {
        val error = NoSuchElementException("not found")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(error)

        val result = useCase.delete(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify(exactly = 0) { sessionRepository.deleteSession(any()) }
    }

    @Test
    fun `delete propagates repository deleteSession failure`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        val error = RuntimeException("db error")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { sessionRepository.deleteSession(SESSION_ID) } returns Result.failure(error)

        val result = useCase.delete(SESSION_ID)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
