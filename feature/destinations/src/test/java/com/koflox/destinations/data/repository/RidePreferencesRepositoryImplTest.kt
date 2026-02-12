package com.koflox.destinations.data.repository

import app.cash.turbine.test
import com.koflox.destinations.data.source.local.RidingModeLocalDataSource
import com.koflox.destinations.domain.model.RidingMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RidePreferencesRepositoryImplTest {

    private val localDataSource: RidingModeLocalDataSource = mockk()
    private lateinit var repository: RidePreferencesRepositoryImpl

    @Before
    fun setup() {
        repository = RidePreferencesRepositoryImpl(localDataSource = localDataSource)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeRidingMode delegates to localDataSource`() = runTest {
        every { localDataSource.observeRidingMode() } returns flowOf(RidingMode.FREE_ROAM)

        repository.observeRidingMode()

        verify(exactly = 1) { localDataSource.observeRidingMode() }
    }

    @Test
    fun `observeRidingMode returns flow from localDataSource`() = runTest {
        every { localDataSource.observeRidingMode() } returns flowOf(RidingMode.DESTINATION)

        repository.observeRidingMode().test {
            assertEquals(RidingMode.DESTINATION, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRidingMode delegates to localDataSource`() = runTest {
        coEvery { localDataSource.setRidingMode(any()) } returns Unit

        repository.setRidingMode(RidingMode.FREE_ROAM)

        coVerify(exactly = 1) { localDataSource.setRidingMode(RidingMode.FREE_ROAM) }
    }

    @Test
    fun `setRidingMode passes DESTINATION mode to localDataSource`() = runTest {
        coEvery { localDataSource.setRidingMode(any()) } returns Unit

        repository.setRidingMode(RidingMode.DESTINATION)

        coVerify(exactly = 1) { localDataSource.setRidingMode(RidingMode.DESTINATION) }
    }
}
