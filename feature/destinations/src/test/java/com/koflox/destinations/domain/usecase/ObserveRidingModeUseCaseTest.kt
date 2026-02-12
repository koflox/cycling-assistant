package com.koflox.destinations.domain.usecase

import app.cash.turbine.test
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveRidingModeUseCaseTest {

    private val repository: RidePreferencesRepository = mockk()
    private lateinit var useCase: ObserveRidingModeUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveRidingModeUseCaseImpl(repository = repository)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observe delegates to repository`() = runTest {
        every { repository.observeRidingMode() } returns flowOf(RidingMode.FREE_ROAM)

        useCase.observe()

        verify(exactly = 1) { repository.observeRidingMode() }
    }

    @Test
    fun `observe emits FREE_ROAM mode`() = runTest {
        every { repository.observeRidingMode() } returns flowOf(RidingMode.FREE_ROAM)

        useCase.observe().test {
            assertEquals(RidingMode.FREE_ROAM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits DESTINATION mode`() = runTest {
        every { repository.observeRidingMode() } returns flowOf(RidingMode.DESTINATION)

        useCase.observe().test {
            assertEquals(RidingMode.DESTINATION, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits mode changes`() = runTest {
        every { repository.observeRidingMode() } returns flowOf(
            RidingMode.FREE_ROAM,
            RidingMode.DESTINATION,
            RidingMode.FREE_ROAM,
        )

        useCase.observe().test {
            assertEquals(RidingMode.FREE_ROAM, awaitItem())
            assertEquals(RidingMode.DESTINATION, awaitItem())
            assertEquals(RidingMode.FREE_ROAM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
