package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.destinations.testutil.createDestination
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetNearbyDestinationsUseCaseTest {

    companion object {
        private const val USER_LAT = 35.6812
        private const val USER_LON = 139.7671
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DestinationsRepository = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var useCase: GetNearbyDestinationsUseCaseImpl

    private val userLocation = Location(latitude = USER_LAT, longitude = USER_LON)

    @Before
    fun setup() {
        useCase = GetNearbyDestinationsUseCaseImpl(
            repository = repository,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `getDestinations queries repository with bounding box`() = runTest {
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(emptyList())

        useCase.getDestinations(userLocation, minDistanceKm = 0.0, maxDistanceKm = 50.0)

        coVerify { repository.getDestinationsInArea(any(), any(), any(), any()) }
    }

    @Test
    fun `getDestinations returns destinations within exact distance range`() = runTest {
        val destination = createDestination(id = "1", latitude = 35.5, longitude = 139.5)
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(destination, result.getOrNull()?.get(0))
    }

    @Test
    fun `getDestinations excludes destinations outside distance range`() = runTest {
        val nearDest = createDestination(id = "near", latitude = 35.5, longitude = 139.5)
        val farDest = createDestination(id = "far", latitude = 34.0, longitude = 138.0)
        coEvery {
            repository.getDestinationsInArea(any(), any(), any(), any())
        } returns Result.success(listOf(nearDest, farDest))
        every { distanceCalculator.calculateKm(any(), any(), eq(35.5), eq(139.5)) } returns 15.0
        every { distanceCalculator.calculateKm(any(), any(), eq(34.0), eq(138.0)) } returns 25.0

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(nearDest, result.getOrNull()?.get(0))
    }

    @Test
    fun `getDestinations returns empty list when no destinations in range`() = runTest {
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(emptyList())

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `getDestinations propagates repository failure`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.failure(exception)

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getDestinations uses correct coordinates for distance calculation`() = runTest {
        val destination = createDestination(id = "1", latitude = 35.5, longitude = 139.5)
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        verify {
            distanceCalculator.calculateKm(
                lat1 = USER_LAT,
                lon1 = USER_LON,
                lat2 = 35.5,
                lon2 = 139.5,
            )
        }
    }

    @Test
    fun `getDestinations includes destination at lower boundary`() = runTest {
        val destination = createDestination(id = "1", latitude = 35.5, longitude = 139.5)
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 10.0

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `getDestinations includes destination at upper boundary`() = runTest {
        val destination = createDestination(id = "1", latitude = 35.5, longitude = 139.5)
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 20.0

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `getDestinations excludes destination just outside range`() = runTest {
        val destination = createDestination(id = "1", latitude = 35.5, longitude = 139.5)
        coEvery { repository.getDestinationsInArea(any(), any(), any(), any()) } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 20.1

        val result = useCase.getDestinations(userLocation, minDistanceKm = 10.0, maxDistanceKm = 20.0)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }
}
