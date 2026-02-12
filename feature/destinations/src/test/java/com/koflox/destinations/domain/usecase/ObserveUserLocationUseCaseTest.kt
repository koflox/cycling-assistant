package com.koflox.destinations.domain.usecase

import app.cash.turbine.test
import com.koflox.destinations.domain.repository.UserLocationRepository
import com.koflox.location.model.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveUserLocationUseCaseTest {

    companion object {
        private const val DEFAULT_LAT = 52.52
        private const val DEFAULT_LONG = 13.405
    }

    private val repository: UserLocationRepository = mockk()
    private lateinit var useCase: ObserveUserLocationUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveUserLocationUseCaseImpl(repository)
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observe delegates to repository`() = runTest {
        val location = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        every { repository.observeUserLocation() } returns flowOf(location)

        useCase.observe()
            .collect()

        verify(exactly = 1) {
            repository.observeUserLocation()
        }
    }

    @Test
    fun `observe emits location from repository`() = runTest {
        val expectedLocation = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        every { repository.observeUserLocation() } returns flowOf(expectedLocation)

        useCase.observe().test {
            assertEquals(expectedLocation, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe emits multiple locations`() = runTest {
        val location1 = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        val location2 = Location(latitude = DEFAULT_LAT + 0.01, longitude = DEFAULT_LONG + 0.01)
        val location3 = Location(latitude = DEFAULT_LAT + 0.02, longitude = DEFAULT_LONG + 0.02)
        every { repository.observeUserLocation() } returns flowOf(location1, location2, location3)

        useCase.observe().test {
            assertEquals(location1, awaitItem())
            assertEquals(location2, awaitItem())
            assertEquals(location3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe emits correct coordinates`() = runTest {
        val lat = -33.8688
        val long = 151.2093
        val expectedLocation = Location(latitude = lat, longitude = long)
        every { repository.observeUserLocation() } returns flowOf(expectedLocation)

        useCase.observe().test {
            val location = awaitItem()
            assertEquals(lat, location.latitude, 0.0)
            assertEquals(long, location.longitude, 0.0)
            awaitComplete()
        }
    }
}
