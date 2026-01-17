package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.repository.DestinationRepository
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

class GetRandomDestinationUseCaseTest {

    companion object {
        private const val DEFAULT_LON = 13.405
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DestinationRepository = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var useCase: GetRandomDestinationUseCaseImpl

    private val userLocation = Location(latitude = 52.52, longitude = DEFAULT_LON)

    @Before
    fun setup() {
        useCase = GetRandomDestinationUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            repository = repository,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `getDestinations calls repository getAllDestinations`() = runTest {
        val destinations = listOf(createDestination("1", 10.0))
        coEvery { repository.getAllDestinations() } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        coVerify(exactly = 1) { repository.getAllDestinations() }
    }

    @Test
    fun `getDestinations returns success with valid destination in range`() = runTest {
        val destination = createDestination("1", 15.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isSuccess)
        assertEquals(destination, result.getOrNull()?.randomizedDestination)
    }

    @Test
    fun `getDestinations filters destinations within tolerance range`() = runTest {
        val nearDestination = createDestination("1", 10.0)
        val validDestination = createDestination("2", 15.0)
        val farDestination = createDestination("3", 25.0)

        coEvery { repository.getAllDestinations() } returns Result.success(
            listOf(nearDestination, validDestination, farDestination),
        )
        every {
            distanceCalculator.calculateKm(any(), any(), eq(nearDestination.latitude), eq(nearDestination.longitude))
        } returns 10.0
        every {
            distanceCalculator.calculateKm(any(), any(), eq(validDestination.latitude), eq(validDestination.longitude))
        } returns 15.0
        every {
            distanceCalculator.calculateKm(any(), any(), eq(farDestination.latitude), eq(farDestination.longitude))
        } returns 25.0

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = true,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        assertEquals(validDestination, destinations.randomizedDestination)
        assertTrue(destinations.otherValidDestinations.isEmpty())
    }

    @Test
    fun `getDestinations returns multiple valid destinations when in range`() = runTest {
        val destination1 = createDestination("1", 12.0)
        val destination2 = createDestination("2", 14.0)
        val destination3 = createDestination("3", 16.0)

        coEvery { repository.getAllDestinations() } returns Result.success(
            listOf(destination1, destination2, destination3),
        )
        every {
            distanceCalculator.calculateKm(any(), any(), eq(destination1.latitude), eq(destination1.longitude))
        } returns 12.0
        every {
            distanceCalculator.calculateKm(any(), any(), eq(destination2.latitude), eq(destination2.longitude))
        } returns 14.0
        every {
            distanceCalculator.calculateKm(any(), any(), eq(destination3.latitude), eq(destination3.longitude))
        } returns 16.0

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = true,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        val allDestinations = listOf(destinations.randomizedDestination) + destinations.otherValidDestinations
        assertEquals(3, allDestinations.size)
        assertTrue(allDestinations.containsAll(listOf(destination1, destination2, destination3)))
    }

    @Test
    fun `getDestinations throws NoSuitableDestinationException when no valid destinations`() = runTest {
        val farDestination = createDestination("1", 50.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(farDestination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 50.0

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }

    @Test
    fun `getDestinations propagates repository failure`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { repository.getAllDestinations() } returns Result.failure(exception)

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getDestinations calls distanceCalculator for each destination`() = runTest {
        val destinations = listOf(
            createDestination("1", 10.0),
            createDestination("2", 20.0),
            createDestination("3", 30.0),
        )
        coEvery { repository.getAllDestinations() } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 50.0

        useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        verify(exactly = 3) { distanceCalculator.calculateKm(any(), any(), any(), any()) }
    }

    @Test
    fun `getDestinations uses correct coordinates for distance calculation`() = runTest {
        val destination = createDestination("1", 48.0, 11.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        verify {
            distanceCalculator.calculateKm(
                lat1 = userLocation.latitude,
                lon1 = userLocation.longitude,
                lat2 = destination.latitude,
                lon2 = destination.longitude,
            )
        }
    }

    @Test
    fun `getDestinations includes destination at lower tolerance boundary`() = runTest {
        val destination = createDestination("1", 15.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 11.0 // 15 - 4 = 11 (lower bound)

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getDestinations includes destination at upper tolerance boundary`() = runTest {
        val destination = createDestination("1", 15.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 19.0 // 15 + 4 = 19 (upper bound)

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getDestinations excludes destination just outside tolerance range`() = runTest {
        val destination = createDestination("1", 15.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 19.1 // Just above upper bound

        val result = useCase.getDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
            areAllValidDestinationsIncluded = false,
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }

    private fun createDestination(
        id: String,
        latitude: Double,
        longitude: Double = DEFAULT_LON,
    ) = Destination(
        id = id,
        title = "Destination $id",
        latitude = latitude,
        longitude = longitude,
    )
}
