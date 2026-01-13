package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.DestinationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InitializeDatabaseUseCaseTest {

    private val repository: DestinationRepository = mockk()
    private lateinit var useCase: InitializeDatabaseUseCaseImpl

    @Before
    fun setup() {
        useCase = InitializeDatabaseUseCaseImpl(repository)
    }

    @Test
    fun `init delegates to repository`() = runTest {
        coEvery { repository.initializeDatabase() } returns Result.success(Unit)

        useCase.init()

        coVerify(exactly = 1) { repository.initializeDatabase() }
    }

    @Test
    fun `init returns success when repository succeeds`() = runTest {
        coEvery { repository.initializeDatabase() } returns Result.success(Unit)

        val result = useCase.init()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `init returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Database initialization failed")
        coEvery { repository.initializeDatabase() } returns Result.failure(exception)

        val result = useCase.init()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `init can be called multiple times`() = runTest {
        coEvery { repository.initializeDatabase() } returns Result.success(Unit)

        useCase.init()
        useCase.init()

        coVerify(exactly = 2) { repository.initializeDatabase() }
    }
}
