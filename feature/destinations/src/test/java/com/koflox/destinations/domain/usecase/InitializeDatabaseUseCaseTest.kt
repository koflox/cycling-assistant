package com.koflox.destinations.domain.usecase

import app.cash.turbine.test
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.location.model.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InitializeDatabaseUseCaseTest {

    companion object {
        private const val TEST_LAT = 35.6812
        private const val TEST_LON = 139.7671
    }

    private val repository: DestinationRepository = mockk()
    private lateinit var useCase: InitializeDatabaseUseCaseImpl

    @Before
    fun setup() {
        useCase = InitializeDatabaseUseCaseImpl(repository)
    }

    private fun createLocation() = Location(latitude = TEST_LAT, longitude = TEST_LON)

    @Test
    fun `init delegates to repository with location`() = runTest {
        val location = createLocation()
        every { repository.loadDestinationsForLocation(location) } returns flowOf(DestinationLoadingEvent.Completed)

        useCase.init(location).test {
            awaitItem()
            awaitComplete()
        }

        verify(exactly = 1) { repository.loadDestinationsForLocation(location) }
    }

    @Test
    fun `init emits events from repository`() = runTest {
        val location = createLocation()
        every { repository.loadDestinationsForLocation(location) } returns flowOf(
            DestinationLoadingEvent.Loading,
            DestinationLoadingEvent.Completed,
        )

        useCase.init(location).test {
            assertEquals(DestinationLoadingEvent.Loading, awaitItem())
            assertEquals(DestinationLoadingEvent.Completed, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `init emits error when repository fails`() = runTest {
        val location = createLocation()
        val exception = RuntimeException("Database initialization failed")
        every { repository.loadDestinationsForLocation(location) } returns flowOf(
            DestinationLoadingEvent.Loading,
            DestinationLoadingEvent.Error(exception),
        )

        useCase.init(location).test {
            assertEquals(DestinationLoadingEvent.Loading, awaitItem())
            val errorEvent = awaitItem() as DestinationLoadingEvent.Error
            assertEquals(exception, errorEvent.throwable)
            awaitComplete()
        }
    }
}
