package com.koflox.destinations.presentation.destinations.delegate

import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.presentation.destinations.RideMapInternalState
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationDelegateTest {

    companion object {
        private const val INITIAL_LAT = 52.52
        private const val INITIAL_LONG = 13.405
        private const val UPDATED_LAT = 52.53
        private const val UPDATED_LONG = 13.41
        private const val FAR_LAT = 53.0
        private const val FAR_LONG = 14.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testScope = TestScope(mainDispatcherRule.testDispatcher)
    private val getUserLocationUseCase: GetUserLocationUseCase = mockk()
    private val observeUserLocationUseCase: ObserveUserLocationUseCase = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()

    private lateinit var uiState: MutableStateFlow<RideMapInternalState>
    private var lastLocationUpdate: Location? = null
    private lateinit var delegate: LocationDelegate

    @Before
    fun setup() {
        uiState = MutableStateFlow(RideMapInternalState())
        lastLocationUpdate = null
        delegate = LocationDelegate(
            getUserLocationUseCase = getUserLocationUseCase,
            observeUserLocationUseCase = observeUserLocationUseCase,
            distanceCalculator = distanceCalculator,
            uiState = uiState,
            scope = testScope,
            onLocationUpdated = { lastLocationUpdate = it },
        )
    }

    @Test
    fun `fetchInitialLocation returns location on success`() = runTest {
        val location = Location(INITIAL_LAT, INITIAL_LONG)
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(location)

        val result = delegate.fetchInitialLocation()

        assertEquals(location, result)
    }

    @Test
    fun `fetchInitialLocation updates uiState with location`() = runTest {
        val location = Location(INITIAL_LAT, INITIAL_LONG)
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(location)

        delegate.fetchInitialLocation()

        assertEquals(location, uiState.value.userLocation)
    }

    @Test
    fun `fetchInitialLocation sets cameraFocusLocation when null`() = runTest {
        val location = Location(INITIAL_LAT, INITIAL_LONG)
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(location)

        delegate.fetchInitialLocation()

        assertEquals(location, uiState.value.cameraFocusLocation)
    }

    @Test
    fun `fetchInitialLocation preserves existing cameraFocusLocation`() = runTest {
        val existingFocus = Location(FAR_LAT, FAR_LONG)
        uiState.value = uiState.value.copy(cameraFocusLocation = existingFocus)
        val location = Location(INITIAL_LAT, INITIAL_LONG)
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(location)

        delegate.fetchInitialLocation()

        assertEquals(existingFocus, uiState.value.cameraFocusLocation)
    }

    @Test
    fun `fetchInitialLocation returns null on failure`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("GPS error"))

        val result = delegate.fetchInitialLocation()

        assertNull(result)
    }

    @Test
    fun `fetchInitialLocation does not update uiState on failure`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("GPS error"))

        delegate.fetchInitialLocation()

        assertNull(uiState.value.userLocation)
    }

    @Test
    fun `startLocationObservation updates userLocation from flow`() = runTest {
        val location = Location(UPDATED_LAT, UPDATED_LONG)
        every { observeUserLocationUseCase.observe() } returns flowOf(location)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.1

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(location, uiState.value.userLocation)
    }

    @Test
    fun `startLocationObservation calls onLocationUpdated callback`() = runTest {
        val location = Location(UPDATED_LAT, UPDATED_LONG)
        every { observeUserLocationUseCase.observe() } returns flowOf(location)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.1

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(location, lastLocationUpdate)
    }

    @Test
    fun `startLocationObservation moves camera when distance exceeds threshold`() = runTest {
        val initialFocus = Location(INITIAL_LAT, INITIAL_LONG)
        uiState.value = uiState.value.copy(cameraFocusLocation = initialFocus)
        val farLocation = Location(FAR_LAT, FAR_LONG)
        every { observeUserLocationUseCase.observe() } returns flowOf(farLocation)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.060

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(farLocation, uiState.value.cameraFocusLocation)
    }

    @Test
    fun `startLocationObservation does not move camera when distance below threshold`() = runTest {
        val initialFocus = Location(INITIAL_LAT, INITIAL_LONG)
        uiState.value = uiState.value.copy(cameraFocusLocation = initialFocus)
        val nearLocation = Location(UPDATED_LAT, UPDATED_LONG)
        every { observeUserLocationUseCase.observe() } returns flowOf(nearLocation)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.010

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(initialFocus, uiState.value.cameraFocusLocation)
    }

    @Test
    fun `startLocationObservation moves camera when cameraFocusLocation is null`() = runTest {
        val location = Location(UPDATED_LAT, UPDATED_LONG)
        every { observeUserLocationUseCase.observe() } returns flowOf(location)

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(location, uiState.value.cameraFocusLocation)
    }

    @Test
    fun `stopLocationObservation cancels observation`() = runTest {
        val locationFlow = MutableStateFlow(Location(INITIAL_LAT, INITIAL_LONG))
        every { observeUserLocationUseCase.observe() } returns locationFlow
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.001

        delegate.startLocationObservation()
        advanceUntilIdle()
        assertNotNull(uiState.value.userLocation)

        delegate.stopLocationObservation()
        lastLocationUpdate = null
        locationFlow.value = Location(FAR_LAT, FAR_LONG)
        advanceUntilIdle()

        assertNull(lastLocationUpdate)
    }

    @Test
    fun `startLocationObservation cancels previous observation`() = runTest {
        val flow1 = MutableStateFlow(Location(INITIAL_LAT, INITIAL_LONG))
        val flow2 = flowOf(Location(UPDATED_LAT, UPDATED_LONG))
        every { observeUserLocationUseCase.observe() } returnsMany listOf(flow1, flow2)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.001

        delegate.startLocationObservation()
        advanceUntilIdle()

        delegate.startLocationObservation()
        advanceUntilIdle()

        assertEquals(UPDATED_LAT, uiState.value.userLocation?.latitude)
    }
}
