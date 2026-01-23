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
import io.mockk.verify
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
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DestinationRepository = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var useCase: GetDestinationInfoUseCaseImpl

    private val userLocation = Location(latitude = 52.52, longitude = DEFAULT_LON)

    @Before
    fun setup() {
        useCase = GetDestinationInfoUseCaseImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            repository = repository,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `getRandomDestinations calls repository getAllDestinations`() = runTest {
        val destinations = listOf(createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 10.0, longitude = DEFAULT_LON))
        coEvery { repository.getAllDestinations() } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        coVerify(exactly = 1) { repository.getAllDestinations() }
    }

    @Test
    fun `getRandomDestinations returns success with valid destination in range`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 15.0, longitude = DEFAULT_LON)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isSuccess)
        assertEquals(destination, result.getOrNull()?.mainDestination)
    }

    @Test
    fun `getRandomDestinations filters destinations within tolerance range`() = runTest {
        val nearDestination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 10.0, longitude = DEFAULT_LON)
        val validDestination = createDestination(id = "2", title = "$TEST_TITLE_PREFIX 2", latitude = 15.0, longitude = DEFAULT_LON)
        val farDestination = createDestination(id = "3", title = "$TEST_TITLE_PREFIX 3", latitude = 25.0, longitude = DEFAULT_LON)

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

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        assertEquals(validDestination, destinations.mainDestination)
        assertTrue(destinations.otherValidDestinations.isEmpty())
    }

    @Test
    fun `getRandomDestinations returns multiple valid destinations when in range`() = runTest {
        val destination1 = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 12.0, longitude = DEFAULT_LON)
        val destination2 = createDestination(id = "2", title = "$TEST_TITLE_PREFIX 2", latitude = 14.0, longitude = DEFAULT_LON)
        val destination3 = createDestination(id = "3", title = "$TEST_TITLE_PREFIX 3", latitude = 16.0, longitude = DEFAULT_LON)

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

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isSuccess)
        val destinations = result.getOrNull()!!
        val allDestinations = listOf(destinations.mainDestination) + destinations.otherValidDestinations
        assertEquals(3, allDestinations.size)
        assertTrue(allDestinations.containsAll(listOf(destination1, destination2, destination3)))
    }

    @Test
    fun `getRandomDestinations throws NoSuitableDestinationException when no valid destinations`() = runTest {
        val farDestination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 50.0, longitude = DEFAULT_LON)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(farDestination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 50.0

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }

    @Test
    fun `getRandomDestinations propagates repository failure`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { repository.getAllDestinations() } returns Result.failure(exception)

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getRandomDestinations calls distanceCalculator for each destination`() = runTest {
        val destinations = listOf(
            createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 10.0, longitude = DEFAULT_LON),
            createDestination(id = "2", title = "$TEST_TITLE_PREFIX 2", latitude = 20.0, longitude = DEFAULT_LON),
            createDestination(id = "3", title = "$TEST_TITLE_PREFIX 3", latitude = 30.0, longitude = DEFAULT_LON),
        )
        coEvery { repository.getAllDestinations() } returns Result.success(destinations)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 50.0

        useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        verify(exactly = 3) { distanceCalculator.calculateKm(any(), any(), any(), any()) }
    }

    @Test
    fun `getRandomDestinations uses correct coordinates for distance calculation`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 48.0, longitude = 11.0)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 15.0

        useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
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
    fun `getRandomDestinations includes destination at lower tolerance boundary`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 15.0, longitude = DEFAULT_LON)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 11.0 // 15 - 4 = 11 (lower bound)

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getRandomDestinations includes destination at upper tolerance boundary`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 15.0, longitude = DEFAULT_LON)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 19.0 // 15 + 4 = 19 (upper bound)

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getRandomDestinations excludes destination just outside tolerance range`() = runTest {
        val destination = createDestination(id = "1", title = "$TEST_TITLE_PREFIX 1", latitude = 15.0, longitude = DEFAULT_LON)
        coEvery { repository.getAllDestinations() } returns Result.success(listOf(destination))
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 19.1 // Just above upper bound

        val result = useCase.getRandomDestinations(
            userLocation = userLocation,
            targetDistanceKm = 15.0,
            toleranceKm = 4.0,
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuitableDestinationException)
    }
}
