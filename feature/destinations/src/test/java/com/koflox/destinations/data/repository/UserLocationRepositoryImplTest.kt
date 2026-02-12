package com.koflox.destinations.data.repository

import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserLocationRepositoryImplTest {

    companion object {
        private const val TEST_LAT = 52.52
        private const val TEST_LONG = 13.405
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationDataSource: LocationDataSource = mockk()
    private lateinit var repository: UserLocationRepositoryImpl

    @Before
    fun setup() {
        repository = UserLocationRepositoryImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            locationDataSource = locationDataSource,
        )
    }

    @Test
    fun `getUserLocation delegates to locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { locationDataSource.getCurrentLocation() } returns Result.success(location)

        repository.getUserLocation()

        coVerify { locationDataSource.getCurrentLocation() }
    }

    @Test
    fun `getUserLocation returns location on success`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { locationDataSource.getCurrentLocation() } returns Result.success(location)

        val result = repository.getUserLocation()

        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }

    @Test
    fun `getUserLocation returns failure on error`() = runTest {
        val exception = RuntimeException("Location unavailable")
        coEvery { locationDataSource.getCurrentLocation() } returns Result.failure(exception)

        val result = repository.getUserLocation()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeUserLocation delegates to locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        every { locationDataSource.observeLocationUpdates(any(), any()) } returns flowOf(location)

        repository.observeUserLocation()

        verify { locationDataSource.observeLocationUpdates() }
    }

    @Test
    fun `observeUserLocation returns flow from locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val flow = flowOf(location)
        every { locationDataSource.observeLocationUpdates(any(), any()) } returns flow

        val result = repository.observeUserLocation()

        assertEquals(flow, result)
    }
}
