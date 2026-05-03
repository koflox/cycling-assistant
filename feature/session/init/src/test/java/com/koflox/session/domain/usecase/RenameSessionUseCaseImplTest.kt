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

class RenameSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val NEW_NAME = "Evening ride"
    }

    private val sessionRepository: SessionRepository = mockk()
    private lateinit var useCase: RenameSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = RenameSessionUseCaseImpl(sessionRepository)
    }

    @Test
    fun `rename completed session delegates to repository`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { sessionRepository.renameSession(SESSION_ID, NEW_NAME) } returns Result.success(Unit)

        val result = useCase.rename(SESSION_ID, NEW_NAME)

        assertTrue(result.isSuccess)
        coVerify { sessionRepository.renameSession(SESSION_ID, NEW_NAME) }
    }

    @Test
    fun `rename trims whitespace before passing to repository`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { sessionRepository.renameSession(SESSION_ID, NEW_NAME) } returns Result.success(Unit)

        useCase.rename(SESSION_ID, "  $NEW_NAME  ")

        coVerify { sessionRepository.renameSession(SESSION_ID, NEW_NAME) }
    }

    @Test
    fun `rename running session fails with SessionNotRenamableException`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.RUNNING)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.rename(SESSION_ID, NEW_NAME)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SessionNotRenamableException)
        assertEquals(SessionStatus.RUNNING, (error as SessionNotRenamableException).status)
        coVerify(exactly = 0) { sessionRepository.renameSession(any(), any()) }
    }

    @Test
    fun `rename paused session fails with SessionNotRenamableException`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.PAUSED)
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)

        val result = useCase.rename(SESSION_ID, NEW_NAME)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SessionNotRenamableException)
        coVerify(exactly = 0) { sessionRepository.renameSession(any(), any()) }
    }

    @Test
    fun `rename propagates getSession failure`() = runTest {
        val error = NoSuchElementException("not found")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.failure(error)

        val result = useCase.rename(SESSION_ID, NEW_NAME)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify(exactly = 0) { sessionRepository.renameSession(any(), any()) }
    }

    @Test
    fun `rename propagates repository renameSession failure`() = runTest {
        val session = createSession(id = SESSION_ID, status = SessionStatus.COMPLETED)
        val error = RuntimeException("db error")
        coEvery { sessionRepository.getSession(SESSION_ID) } returns Result.success(session)
        coEvery { sessionRepository.renameSession(SESSION_ID, NEW_NAME) } returns Result.failure(error)

        val result = useCase.rename(SESSION_ID, NEW_NAME)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
