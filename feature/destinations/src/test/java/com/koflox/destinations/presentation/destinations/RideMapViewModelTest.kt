package com.koflox.destinations.presentation.destinations

import android.app.Application
import app.cash.turbine.test
import com.koflox.destinationnutrition.bridge.model.NutritionBreakEvent
import com.koflox.destinationnutrition.bridge.usecase.ObserveNutritionBreakUseCase
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.model.DistanceBounds
import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.usecase.CheckLocationEnabledUseCase
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.ObserveRidingModeUseCase
import com.koflox.destinations.domain.usecase.ObserveUserLocationUseCase
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.domain.usecase.UpdateRidingModeUseCase
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.destinationsession.bridge.usecase.CyclingSessionUseCase
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideMapViewModelTest {

    companion object {
        private const val USER_LAT = 52.52
        private const val USER_LONG = 13.405
        private const val ERROR_MESSAGE = "Something went wrong"
        private const val NUTRITION_SUGGESTION_MS = 3600000L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase = mockk()
    private val getUserLocationUseCase: GetUserLocationUseCase = mockk()
    private val observeUserLocationUseCase: ObserveUserLocationUseCase = mockk()
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase = mockk()
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase = mockk()
    private val getDistanceBoundsUseCase: GetDistanceBoundsUseCase = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private val uiMapper: DestinationUiMapper = mockk()
    private val toleranceCalculator: ToleranceCalculator = mockk()
    private val application: Application = mockk(relaxed = true)
    private val cyclingSessionUseCase: CyclingSessionUseCase = mockk()
    private val observeNutritionBreakUseCase: ObserveNutritionBreakUseCase = mockk()
    private val observeRidingModeUseCase: ObserveRidingModeUseCase = mockk()
    private val updateRidingModeUseCase: UpdateRidingModeUseCase = mockk()

    private val locationEnabledFlow = MutableStateFlow(true)
    private val ridingModeFlow = MutableStateFlow(RidingMode.FREE_ROAM)
    private val activeSessionFlow = MutableStateFlow(false)
    private val nutritionFlow = MutableSharedFlow<NutritionBreakEvent>()

    private lateinit var viewModel: RideMapViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { checkLocationEnabledUseCase.observeLocationEnabled() } returns locationEnabledFlow
        every { observeRidingModeUseCase.observe() } returns ridingModeFlow
        coEvery { getUserLocationUseCase.getLocation() } returns Result.success(createUserLocation())
        every { cyclingSessionUseCase.observeHasActiveSession() } returns activeSessionFlow
        coEvery { cyclingSessionUseCase.getActiveSessionDestination() } returns null
        every { observeNutritionBreakUseCase.observeNutritionBreakEvents() } returns nutritionFlow
        every { application.getString(any()) } returns ERROR_MESSAGE
        every { application.getString(any(), any()) } returns ERROR_MESSAGE
        every { initializeDatabaseUseCase.init(any()) } returns flowOf(
            DestinationLoadingEvent.Loading,
            DestinationLoadingEvent.Completed,
        )
        coEvery { getDistanceBoundsUseCase.getBounds(any()) } returns Result.success(
            DistanceBounds(minKm = 2.0, maxKm = 50.0),
        )
        every { toleranceCalculator.calculateKm(any()) } returns 3.0
    }

    private fun createViewModel(): RideMapViewModel = RideMapViewModel(
        checkLocationEnabledUseCase = checkLocationEnabledUseCase,
        getUserLocationUseCase = getUserLocationUseCase,
        observeUserLocationUseCase = observeUserLocationUseCase,
        initializeDatabaseUseCase = initializeDatabaseUseCase,
        getDestinationInfoUseCase = getDestinationInfoUseCase,
        getDistanceBoundsUseCase = getDistanceBoundsUseCase,
        distanceCalculator = distanceCalculator,
        uiMapper = uiMapper,
        toleranceCalculator = toleranceCalculator,
        application = application,
        cyclingSessionUseCase = cyclingSessionUseCase,
        observeNutritionBreakUseCase = observeNutritionBreakUseCase,
        observeRidingModeUseCase = observeRidingModeUseCase,
        updateRidingModeUseCase = updateRidingModeUseCase,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is RideMapUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state becomes FreeRoamIdle after permission granted`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Loading

            sendMapLoaded()
            viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue("Expected FreeRoamIdle but was $state", state is RideMapUiState.FreeRoamIdle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FreeRoamIdle shows user location`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RideMapUiState.FreeRoamIdle)
        assertEquals(createUserLocation(), (state as RideMapUiState.FreeRoamIdle).userLocation)
    }

    @Test
    fun `PermissionDenied shows PermissionDenied state with rationale`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionDenied(isRationaleAvailable = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected PermissionDenied but was $state", state is RideMapUiState.PermissionDenied)
        assertTrue((state as RideMapUiState.PermissionDenied).isRationaleAvailable)
    }

    @Test
    fun `PermissionDenied permanently shows PermissionDenied state without rationale`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionDenied(isRationaleAvailable = false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected PermissionDenied but was $state", state is RideMapUiState.PermissionDenied)
        assertEquals(false, (state as RideMapUiState.PermissionDenied).isRationaleAvailable)
    }

    @Test
    fun `PermissionGranted after denial clears denied state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionDenied(isRationaleAvailable = true))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is RideMapUiState.PermissionDenied)

        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FreeRoamIdle but was $state", state is RideMapUiState.FreeRoamIdle)
    }

    @Test
    fun `ErrorDismissed clears error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.CommonEvent.ErrorDismissed)
        advanceUntilIdle()

        val state = viewModel.uiState.value as? RideMapUiState.FreeRoamIdle
        assertNull(state?.error)
    }

    @Test
    fun `LocationDisabled state when location off and no session and no location`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("No GPS"))
        locationEnabledFlow.value = false

        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        assertEquals(RideMapUiState.LocationDisabled, viewModel.uiState.value)
    }

    @Test
    fun `active session shows ActiveSession state`() = runTest {
        activeSessionFlow.value = true

        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RideMapUiState.ActiveSession)
    }

    @Test
    fun `session ending clears selected destination`() = runTest {
        activeSessionFlow.value = true

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is RideMapUiState.ActiveSession)

        activeSessionFlow.value = false
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RideMapUiState.FreeRoamIdle)
    }

    @Test
    fun `startFreeRoamSession success transitions to active session`() = runTest {
        coEvery { cyclingSessionUseCase.startFreeRoamSession() } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.SessionEvent.StartFreeRoamClicked)
        advanceUntilIdle()

        coVerify { cyclingSessionUseCase.startFreeRoamSession() }
    }

    @Test
    fun `startFreeRoamSession failure shows error`() = runTest {
        coEvery { cyclingSessionUseCase.startFreeRoamSession() } returns Result.failure(RuntimeException("error"))

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.SessionEvent.StartFreeRoamClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as RideMapUiState.FreeRoamIdle
        assertNotNull(state.error)
    }

    @Test
    fun `ModeSelected to DESTINATION delegates to updateRidingModeUseCase`() = runTest {
        coEvery { updateRidingModeUseCase.update(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.ModeEvent.ModeSelected(RidingMode.DESTINATION))
        advanceUntilIdle()

        coVerify { updateRidingModeUseCase.update(RidingMode.DESTINATION) }
    }

    @Test
    fun `observeRidingMode updates internal state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RideMapUiState.FreeRoamIdle)

        ridingModeFlow.value = RidingMode.DESTINATION
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RideMapUiState.DestinationIdle)
    }

    @Test
    fun `NutritionPopupDismissed clears nutrition suggestion`() = runTest {
        activeSessionFlow.value = true
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        nutritionFlow.emit(NutritionBreakEvent.BreakRequired(NUTRITION_SUGGESTION_MS))
        advanceUntilIdle()

        val activeState = viewModel.uiState.value as RideMapUiState.ActiveSession
        assertEquals(NUTRITION_SUGGESTION_MS, activeState.nutritionSuggestionTimeMs)

        viewModel.onEvent(RideMapUiEvent.CommonEvent.NutritionPopupDismissed)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value as RideMapUiState.ActiveSession
        assertNull(updatedState.nutritionSuggestionTimeMs)
    }

    @Test
    fun `NavigationActionHandled clears navigation action`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.CommonEvent.NavigationActionHandled)
        advanceUntilIdle()

        val state = viewModel.uiState.value as RideMapUiState.FreeRoamIdle
        assertNull(state.error)
    }

    @Test
    fun `ScreenResumed with permission starts location observation`() = runTest {
        every { observeUserLocationUseCase.observe() } returns emptyFlow()

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenResumed)
        advanceUntilIdle()
    }

    @Test
    fun `ScreenPaused stops location observation`() = runTest {
        every { observeUserLocationUseCase.observe() } returns emptyFlow()

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenResumed)
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenPaused)
        advanceUntilIdle()
    }

    @Test
    fun `RetryInitializationClicked re-initializes`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RideMapUiEvent.LifecycleEvent.RetryInitializationClicked)
        advanceUntilIdle()

        coVerify(atLeast = 2) { getUserLocationUseCase.getLocation() }
    }

    @Test
    fun `ActiveSession in free roam mode has null destination`() = runTest {
        activeSessionFlow.value = true

        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value as RideMapUiState.ActiveSession
        assertNull(state.selectedDestination)
        assertTrue(state.curvePoints.isEmpty())
    }

    @Test
    fun `startFreeRoamSession not starting shows no loading indicator`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value as RideMapUiState.FreeRoamIdle
        assertEquals(false, state.isStartingFreeRoam)
    }

    @Test
    fun `initial location fetch failure stays Loading after permission and map loaded`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("No GPS"))

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Loading but was $state", state is RideMapUiState.Loading)
    }

    @Test
    fun `initial location fetch failure transitions after observation delivers location`() = runTest {
        coEvery { getUserLocationUseCase.getLocation() } returns Result.failure(RuntimeException("No GPS"))
        val locationFlow = MutableSharedFlow<Location>()
        every { observeUserLocationUseCase.observe() } returns locationFlow

        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.LifecycleEvent.ScreenResumed)
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is RideMapUiState.Loading)

        locationFlow.emit(createUserLocation())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FreeRoamIdle but was $state", state is RideMapUiState.FreeRoamIdle)
        assertEquals(createUserLocation(), (state as RideMapUiState.FreeRoamIdle).cameraFocusLocation)
    }

    @Test
    fun `FreeRoamIdle always has non-null cameraFocusLocation`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        sendMapLoaded()
        viewModel.onEvent(RideMapUiEvent.PermissionEvent.PermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value as RideMapUiState.FreeRoamIdle
        assertNotNull(state.cameraFocusLocation)
    }

    private fun sendMapLoaded() {
        viewModel.onEvent(RideMapUiEvent.MapEvent.MapLoaded)
    }

    private fun createUserLocation() = Location(USER_LAT, USER_LONG)
}
