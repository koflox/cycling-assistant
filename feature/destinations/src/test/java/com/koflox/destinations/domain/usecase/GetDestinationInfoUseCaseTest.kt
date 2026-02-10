package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.destinations.testutil.createDestination
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetDestinationInfoUseCaseTest {

    companion object {
        private const val DEFAULT_LON = 13.405
        private const val TEST_TITLE_PREFIX = "Destination "
        private const val TOLERANCE_KM = 4.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DestinationRepository = mockk()
    private val getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private val toleranceCalculator: ToleranceCalculator = mockk()
    private lateinit var useCase: GetDestinationInfoUseCaseImpl

    private val userLocation = Location(latitude = 52.52, longitude = DEFAULT_LON)

    @Before
    fun setup() {
        every { toleranceCalculator.calculateKm(any()) } returns TOLERANCE_KM
        useCase = GetDestinationInfoUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            repository = repository,
            getNearbyDestinationsUseCase = getNearbyDestinationsUseCase,
            distanceCalculator = distanceCalculator,
            toleranceCalculator = toleranceCalculator,
        )
    }

    @Test
    fun `getRandomDestinations calls getNearbyDestinationsUseCase`() = runTest {
        val destinations = listOf(createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 10.0, longitude = DEFAULT_LON))
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(destinations)

        useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        coVerify(exactly = 1) {
            getNearbyDestinationsUseCase.getDestinations(
                userLocation = userLocation,
                minDistanceKm = 11.0,
                maxDistanceKm = 19.0,
            )
        }
    }

    @Test
    fun `getRandomDestinations returns success with valid destination`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 15.0, longitude = DEFAULT_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(any(), any(), any())
        } returns Result.success(listOf(destination))

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        assertTrue(result.isSuccess)
        assertEquals(destination, result.getOrNull()?.mainDestination)
    }

    @Test
    fun `getRandomDestinations returns multiple valid destinations`() = runTest {
        val destination1 = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 12.0, longitude = DEFAULT_LON)
        val destination2 = createDestination(id = "2", title = "$TEST_TITLE_PREFIX 2", latitude = 14.0, longitude = DEFAULT_LON)
        val destination3 = createDestination(id = "3", title = "$TEST_TITLE_PREFIX 3", latitude = 16.0, longitude = DEFAULT_LON)

        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(
            listOf(destination1, destination2, destination3),
        )

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        val allDestinations = listOf(destinations.mainDestination) + destinations.otherValidDestinations
        assertEquals(3, allDestinations.size)
        assertTrue(allDestinations.containsAll(listOf(destination1, destination2, destination3)))
    }

    @Test
    fun `getRandomDestinations falls back to nearest destination when range is empty`() = runTest {
        val nearDest = createDestination(id = "near", latitude = 10.0, longitude = DEFAULT_LON)
        val farDest = createDestination(id = "far", latitude = 20.0, longitude = DEFAULT_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(userLocation, minDistanceKm = 11.0, maxDistanceKm = 19.0)
        } returns Result.success(emptyList())
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(userLocation, minDistanceKm = 0.0, maxDistanceKm = 150.0)
        } returns Result.success(listOf(nearDest, farDest))
        every { distanceCalculator.calculateKm(any(), any(), eq(10.0), eq(DEFAULT_LON)) } returns 39.0
        every { distanceCalculator.calculateKm(any(), any(), eq(20.0), eq(DEFAULT_LON)) } returns 51.0

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        assertEquals(nearDest, destinations.mainDestination)
        assertTrue(destinations.otherValidDestinations.isEmpty())
    }

    @Test
    fun `getRandomDestinations fallback picks closest to target distance`() = runTest {
        val closerToTarget = createDestination(id = "closer", latitude = 10.0, longitude = DEFAULT_LON)
        val fartherFromTarget = createDestination(id = "farther", latitude = 20.0, longitude = DEFAULT_LON)
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(userLocation, minDistanceKm = 41.0, maxDistanceKm = 49.0)
        } returns Result.success(emptyList())
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(userLocation, minDistanceKm = 0.0, maxDistanceKm = 150.0)
        } returns Result.success(listOf(closerToTarget, fartherFromTarget))
        every { distanceCalculator.calculateKm(any(), any(), eq(10.0), eq(DEFAULT_LON)) } returns 40.0
        every { distanceCalculator.calculateKm(any(), any(), eq(20.0), eq(DEFAULT_LON)) } returns 50.0

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 45.0,
        )

        assertTrue(result.isSuccess)
        assertEquals(closerToTarget, result.getOrNull()?.mainDestination)
    }

    @Test
    fun `getRandomDestinations throws when no destinations at all`() = runTest {
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(emptyList())

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }

    @Test
    fun `getRandomDestinations propagates failure`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.failure(exception)

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
        )

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getRandomDestinations passes correct distance range`() = runTest {
        coEvery { getNearbyDestinationsUseCase.getDestinations(any(), any(), any()) } returns Result.success(
            listOf(createDestination(id = "1")),
        )

        useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 20.0,
        )

        coVerify {
            getNearbyDestinationsUseCase.getDestinations(
                userLocation = userLocation,
                minDistanceKm = 16.0,
                maxDistanceKm = 24.0,
            )
        }
    }

    @Test
    fun `getDestinations by id fetches destination and computes target distance`() = runTest {
        val destination = createDestination(id = "dest-1", title = "Target", latitude = 48.0, longitude = 11.0)
        coEvery { repository.getDestinationById("dest-1") } returns Result.success(destination)
        every {
            distanceCalculator.calculateKm(
                lat1 = userLocation.latitude,
                lon1 = userLocation.longitude,
                lat2 = destination.latitude,
                lon2 = destination.longitude,
            )
        } returns 30.0
        coEvery {
            getNearbyDestinationsUseCase.getDestinations(any(), any(), any())
        } returns Result.success(listOf(destination))

        val result = useCase.getDestinations(
            userLocation = userLocation,
            destinationId = "dest-1",
        )

        assertTrue(result.isSuccess)
        assertEquals(destination, result.getOrNull()?.mainDestination)
        coVerify {
            getNearbyDestinationsUseCase.getDestinations(
                userLocation = userLocation,
                minDistanceKm = 26.0,
                maxDistanceKm = 34.0,
            )
        }
    }

    @Test
    fun `getDestinations by id throws when destination not found`() = runTest {
        coEvery { repository.getDestinationById("missing") } returns Result.success(null)

        val result = runCatching {
            useCase.getDestinations(
                userLocation = userLocation,
                destinationId = "missing",
            )
        }

        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }
}
