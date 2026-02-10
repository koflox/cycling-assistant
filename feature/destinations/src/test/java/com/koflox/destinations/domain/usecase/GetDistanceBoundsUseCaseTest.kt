package com.koflox.destinations.domain.usecase

import com.koflox.destinations.testutil.createDestination
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetDistanceBoundsUseCaseTest {

    companion object {
        private const val USER_LAT = 52.52
        private const val USER_LON = 13.405
        private const val DEST1_LAT = 10.0
        private const val DEST1_LON = 11.0
        private const val DEST2_LAT = 20.0
        private const val DEST2_LON = 21.0
        private const val DEST3_LAT = 30.0
        private const val DEST3_LON = 31.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var useCase: GetDistanceBoundsUseCaseImpl

    private val userLocation = Location(latitude = USER_LAT, longitude = USER_LON)

    @Before
    fun setup() {
        useCase = GetDistanceBoundsUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            getNearbyDestinationsUseCase = getNearbyDestinationsUseCase,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `getBounds returns correct min and max for multiple destinations`() = runTest {
        val destinations = listOf(
            createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON),
            createDestination(id = "2", latitude = DEST2_LAT, longitude = DEST2_LON),
            createDestination(id = "3", latitude = DEST3_LAT, longitude = DEST3_LON),
        )
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST1_LAT), eq(DEST1_LON)) } returns 5.3
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST2_LAT), eq(DEST2_LON)) } returns 12.7
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST3_LAT), eq(DEST3_LON)) } returns 22.1

        val result = useCase.getBounds(userLocation)

        assertTrue(result.isSuccess)
        val bounds = result.getOrNull()!!
        assertEquals(5.0, bounds.minKm, 0.0)
        assertEquals(23.0, bounds.maxKm, 0.0)
    }

    @Test
    fun `getBounds returns null when no nearby destinations`() = runTest {
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(emptyList())

        val result = useCase.getBounds(userLocation)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getBounds propagates failure`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.failure(exception)

        val result = useCase.getBounds(userLocation)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getBounds uses correct coordinates for distance calculation`() = runTest {
        val destination = createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(any(), any(), any())
        } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 10.0

        useCase.getBounds(userLocation)

        verify {
            distanceCalculator.calculateKm(
                lat1 = USER_LAT,
                lon1 = USER_LON,
                lat2 = DEST1_LAT,
                lon2 = DEST1_LON,
            )
        }
    }

    @Test
    fun `getBounds floors min and ceils max to integers`() = runTest {
        val destinations = listOf(
            createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON),
            createDestination(id = "2", latitude = DEST2_LAT, longitude = DEST2_LON),
        )
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST1_LAT), eq(DEST1_LON)) } returns 3.8
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST2_LAT), eq(DEST2_LON)) } returns 17.2

        val result = useCase.getBounds(userLocation)

        val bounds = result.getOrNull()!!
        assertEquals(3.0, bounds.minKm, 0.0)
        assertEquals(18.0, bounds.maxKm, 0.0)
    }

    @Test
    fun `getBounds with single destination returns same floor and ceil`() = runTest {
        val destination = createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(any(), any(), any())
        } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 8.5

        val result = useCase.getBounds(userLocation)

        val bounds = result.getOrNull()!!
        assertEquals(8.0, bounds.minKm, 0.0)
        assertEquals(9.0, bounds.maxKm, 0.0)
    }

    @Test
    fun `getBounds clamps min to minimum bound distance`() = runTest {
        val destination = createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(any(), any(), any())
        } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.3

        val result = useCase.getBounds(userLocation)

        val bounds = result.getOrNull()!!
        assertEquals(1.0, bounds.minKm, 0.0)
        assertEquals(1.0, bounds.maxKm, 0.0)
    }

    @Test
    fun `getBounds with exact integer distances returns those values`() = runTest {
        val destinations = listOf(
            createDestination(id = "1", latitude = DEST1_LAT, longitude = DEST1_LON),
            createDestination(id = "2", latitude = DEST2_LAT, longitude = DEST2_LON),
        )
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST1_LAT), eq(DEST1_LON)) } returns 5.0
        every { distanceCalculator.calculateKm(any(), any(), eq(DEST2_LAT), eq(DEST2_LON)) } returns 20.0

        val result = useCase.getBounds(userLocation)

        val bounds = result.getOrNull()!!
        assertEquals(5.0, bounds.minKm, 0.0)
        assertEquals(20.0, bounds.maxKm, 0.0)
    }
}
