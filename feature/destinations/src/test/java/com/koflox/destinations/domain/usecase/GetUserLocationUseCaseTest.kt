package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.location.model.Location
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserLocationUseCaseTest {

    companion object {
        private const val DEFAULT_LAT = 52.52
        private const val DEFAULT_LONG = 13.405
    }

    private val repository: DestinationRepository = mockk()
    private lateinit var useCase: GetUserLocationUseCaseImpl

    @Before
    fun setup() {
        useCase = GetUserLocationUseCaseImpl(repository)
    }

    @Test
    fun `getLocation delegates to repository`() = runTest {
        val expectedLocation = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        coEvery { repository.getUserLocation() } returns Result.success(expectedLocation)

        useCase.getLocation()

        coVerify(exactly = 1) { repository.getUserLocation() }
    }

    @Test
    fun `getLocation returns success when repository succeeds`() = runTest {
        val expectedLocation = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        coEvery { repository.getUserLocation() } returns Result.success(expectedLocation)

        val result = useCase.getLocation()

        assertTrue(result.isSuccess)
        assertEquals(expectedLocation, result.getOrNull())
    }

    @Test
    fun `getLocation returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Location unavailable")
        coEvery { repository.getUserLocation() } returns Result.failure(exception)

        val result = useCase.getLocation()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getLocation returns correct location coordinates`() = runTest {
        val expectedLocation = Location(latitude = DEFAULT_LAT, longitude = DEFAULT_LONG)
        coEvery { repository.getUserLocation() } returns Result.success(expectedLocation)

        val result = useCase.getLocation()

        val location = result.getOrNull()!!
        assertEquals(DEFAULT_LAT, location.latitude, 0.0)
        assertEquals(DEFAULT_LONG, location.longitude, 0.0)
    }
}
