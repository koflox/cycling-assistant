package com.koflox.destinations.presentation.destinations.delegate

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.koflox.designsystem.text.UiText
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.model.DistanceBounds
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.NoSuitableDestinationException
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.presentation.destinations.NavigationAction
import com.koflox.destinations.presentation.destinations.RideMapInternalState
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.destinations.model.DestinationsUiModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinations.testutil.createDestination
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DestinationDelegateTest {

    companion object {
        private const val USER_LAT = 52.52
        private const val USER_LONG = 13.405
        private const val DEST_LAT = 52.55
        private const val DEST_LONG = 13.45
        private const val DESTINATION_ID = "dest-123"
        private const val DESTINATION_TITLE = "Test Destination"
        private const val ROUTE_DISTANCE_KM = 10.0
        private const val TOLERANCE_KM = 3.2
        private const val MIN_BOUNDS_KM = 2.0
        private const val MAX_BOUNDS_KM = 50.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testScope = TestScope(mainDispatcherRule.testDispatcher)
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase = mockk()
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase = mockk()
    private val getDistanceBoundsUseCase: GetDistanceBoundsUseCase = mockk()
    private val getUserLocationUseCase: GetUserLocationUseCase = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private val uiMapper: DestinationUiMapper = mockk()
    private val toleranceCalculator: ToleranceCalculator = mockk()
    private val application: Application = mockk()
    private val packageManager: PackageManager = mockk()

    private lateinit var uiState: MutableStateFlow<RideMapInternalState>
    private lateinit var delegate: DestinationDelegate

    @Before
    fun setup() {
        uiState = MutableStateFlow(RideMapInternalState())
        every { application.packageManager } returns packageManager
        every { toleranceCalculator.calculateKm(any()) } returns TOLERANCE_KM
        delegate = createDelegate()
    }

    private fun createDelegate() = DestinationDelegate(
        initializeDatabaseUseCase = initializeDatabaseUseCase,
        getDestinationInfoUseCase = getDestinationInfoUseCase,
        getDistanceBoundsUseCase = getDistanceBoundsUseCase,
        getUserLocationUseCase = getUserLocationUseCase,
        distanceCalculator = distanceCalculator,
        uiMapper = uiMapper,
        toleranceCalculator = toleranceCalculator,
        application = application,
        uiState = uiState,
        scope = testScope,
    )

    @Test
    fun `startDestinationLoading sets isPreparingDestinations on loading event`() = runTest {
        every { initializeDatabaseUseCase.init(any()) } returns flowOf(DestinationLoadingEvent.Loading)

        delegate.startDestinationLoading(createUserLocation())
        advanceUntilIdle()

        assertTrue(uiState.value.isPreparingDestinations)
        assertFalse(uiState.value.areDestinationsReady)
    }

    @Test
    fun `startDestinationLoading sets areDestinationsReady on completed event`() = runTest {
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(
            DistanceBounds(MIN_BOUNDS_KM, MAX_BOUNDS_KM),
        )
        every { initializeDatabaseUseCase.init(any()) } returns flowOf(
            DestinationLoadingEvent.Loading,
            DestinationLoadingEvent.Completed,
        )

        delegate.startDestinationLoading(createUserLocation())
        advanceUntilIdle()

        assertFalse(uiState.value.isPreparingDestinations)
        assertTrue(uiState.value.areDestinationsReady)
    }

    @Test
    fun `startDestinationLoading sets error on error event`() = runTest {
        every { initializeDatabaseUseCase.init(any()) } returns flowOf(
            DestinationLoadingEvent.Error(RuntimeException("DB error")),
        )

        delegate.startDestinationLoading(createUserLocation())
        advanceUntilIdle()

        assertNotNull(uiState.value.error)
        assertFalse(uiState.value.isPreparingDestinations)
    }

    @Test
    fun `calculateDistanceBounds updates bounds on success`() = runTest {
        val bounds = DistanceBounds(MIN_BOUNDS_KM, MAX_BOUNDS_KM)
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(bounds)

        delegate.calculateDistanceBounds(createUserLocation())

        assertEquals(bounds, uiState.value.distanceBounds)
        assertFalse(uiState.value.isCalculatingBounds)
    }

    @Test
    fun `calculateDistanceBounds sets initial routeDistance from bounds`() = runTest {
        val bounds = DistanceBounds(MIN_BOUNDS_KM, MAX_BOUNDS_KM)
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(bounds)

        delegate.calculateDistanceBounds(createUserLocation())

        val expectedDistance = (MIN_BOUNDS_KM + MAX_BOUNDS_KM) / 4
        assertEquals(expectedDistance, uiState.value.routeDistanceKm, 0.01)
    }

    @Test
    fun `calculateDistanceBounds coerces existing routeDistance to bounds`() = runTest {
        uiState.value = uiState.value.copy(routeDistanceKm = 100.0)
        val bounds = DistanceBounds(MIN_BOUNDS_KM, MAX_BOUNDS_KM)
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(bounds)

        delegate.calculateDistanceBounds(createUserLocation())

        assertEquals(MAX_BOUNDS_KM, uiState.value.routeDistanceKm, 0.01)
    }

    @Test
    fun `calculateDistanceBounds clears bounds when null result`() = runTest {
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(null)

        delegate.calculateDistanceBounds(createUserLocation())

        assertNull(uiState.value.distanceBounds)
        assertFalse(uiState.value.isCalculatingBounds)
    }

    @Test
    fun `calculateDistanceBounds clears isCalculatingBounds on failure`() = runTest {
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.failure(RuntimeException("error"))

        delegate.calculateDistanceBounds(createUserLocation())

        assertFalse(uiState.value.isCalculatingBounds)
    }

    @Test
    fun `findDestination selects random destination on success`() = runTest {
        val destinations = Destinations(
            mainDestination = createDestination(id = DESTINATION_ID, title = DESTINATION_TITLE),
            otherValidDestinations = emptyList(),
        )
        val uiModel = createDestinationUiModel()
        uiState.value = uiState.value.copy(routeDistanceKm = ROUTE_DISTANCE_KM)
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(createUserLocation())
        coEvery { getDestinationInfoUseCase.getRandomDestinations(any(), any()) } returns Result.success(destinations)
        coEvery { uiMapper.toUiModel(any(), any()) } returns DestinationsUiModel(
            selected = uiModel,
            otherValidDestinations = emptyList(),
        )

        delegate.findDestination()

        assertEquals(uiModel, uiState.value.selectedDestination)
        assertFalse(uiState.value.isLoading)
    }

    @Test
    fun `findDestination sets error on location failure`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("No GPS"))

        delegate.findDestination()

        assertNotNull(uiState.value.error)
        assertFalse(uiState.value.isLoading)
    }

    @Test
    fun `findDestination by id recovers session destination`() = runTest {
        val destinations = Destinations(
            mainDestination = createDestination(id = DESTINATION_ID, title = DESTINATION_TITLE),
            otherValidDestinations = emptyList(),
        )
        val uiModel = createDestinationUiModel()
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(createUserLocation())
        coEvery { getDestinationInfoUseCase.getDestinations(any(), destinationId = DESTINATION_ID) } returns
            Result.success(destinations)
        coEvery { uiMapper.toUiModel(any(), any()) } returns DestinationsUiModel(
            selected = uiModel,
            otherValidDestinations = emptyList(),
        )

        delegate.findDestination(destinationId = DESTINATION_ID)

        assertTrue(uiState.value.isSessionActive)
    }

    @Test
    fun `findDestination handles NoSuitableDestinationException`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(createUserLocation())
        coEvery { getDestinationInfoUseCase.getRandomDestinations(any(), any()) } returns
            Result.failure(NoSuitableDestinationException())

        delegate.findDestination()

        assertNotNull(uiState.value.error)
    }

    @Test
    fun `updateRouteDistance updates state`() = runTest {
        delegate.updateRouteDistance(ROUTE_DISTANCE_KM)

        assertEquals(ROUTE_DISTANCE_KM, uiState.value.routeDistanceKm, 0.0)
        assertEquals(TOLERANCE_KM, uiState.value.toleranceKm, 0.0)
    }

    @Test
    fun `checkAndReloadDestinationsIfNeeded loads when no previous location`() = runTest {
        every { initializeDatabaseUseCase.init(any()) } returns flowOf(DestinationLoadingEvent.Loading)

        delegate.checkAndReloadDestinationsIfNeeded(createUserLocation())
        advanceUntilIdle()

        assertTrue(uiState.value.isPreparingDestinations)
    }

    @Test
    fun `checkAndReloadDestinationsIfNeeded skips when already preparing`() = runTest {
        uiState.value = uiState.value.copy(isPreparingDestinations = true)

        delegate.checkAndReloadDestinationsIfNeeded(createUserLocation())

        assertTrue(uiState.value.isPreparingDestinations)
    }

    @Test
    fun `checkAndRecalculateBoundsIfNeeded skips when already calculating`() = runTest {
        uiState.value = uiState.value.copy(isCalculatingBounds = true)

        delegate.checkAndRecalculateBoundsIfNeeded(createUserLocation())

        assertTrue(uiState.value.isCalculatingBounds)
    }

    @Test
    fun `showMarkerOptionsDialog sets flag to true`() = runTest {
        delegate.showMarkerOptionsDialog()

        assertTrue(uiState.value.showSelectedMarkerOptionsDialog)
    }

    @Test
    fun `dismissSelectedMarkerOptionsDialog sets flag to false`() = runTest {
        uiState.value = uiState.value.copy(showSelectedMarkerOptionsDialog = true)

        delegate.dismissSelectedMarkerOptionsDialog()

        assertFalse(uiState.value.showSelectedMarkerOptionsDialog)
    }

    @Test
    fun `openInGoogleMaps sets navigation action when maps installed`() = runTest {
        mockkStatic(Uri::class)
        try {
            val mockUri: Uri = mockk()
            every { Uri.parse(any()) } returns mockUri
            every { packageManager.getPackageInfo("com.google.android.apps.maps", 0) } returns PackageInfo()

            delegate.openInGoogleMaps(createDestinationUiModel())

            val action = uiState.value.navigationAction
            assertTrue(action is NavigationAction.OpenGoogleMaps)
            assertEquals(mockUri, (action as NavigationAction.OpenGoogleMaps).uri)
        } finally {
            unmockkStatic(Uri::class)
        }
    }

    @Test
    fun `openInGoogleMaps sets error when maps not installed`() = runTest {
        every { packageManager.getPackageInfo("com.google.android.apps.maps", 0) } throws
            PackageManager.NameNotFoundException()

        delegate.openInGoogleMaps(createDestinationUiModel())

        assertNotNull(uiState.value.error)
        assertNull(uiState.value.navigationAction)
    }

    private fun createUserLocation() = Location(USER_LAT, USER_LONG)

    private fun createDestinationUiModel() = DestinationUiModel(
        id = DESTINATION_ID,
        title = DESTINATION_TITLE,
        location = Location(DEST_LAT, DEST_LONG),
        distanceKm = ROUTE_DISTANCE_KM,
        distanceFormatted = UiText.Resource(R.string.distance_to_dest_desc, listOf(ROUTE_DISTANCE_KM)),
        isMain = true,
    )
}
