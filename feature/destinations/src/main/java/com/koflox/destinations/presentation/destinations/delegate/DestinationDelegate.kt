package com.koflox.destinations.presentation.destinations.delegate

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.usecase.GetDestinationInfoUseCase
import com.koflox.destinations.domain.usecase.GetDistanceBoundsUseCase
import com.koflox.destinations.domain.usecase.GetUserLocationUseCase
import com.koflox.destinations.domain.usecase.InitializeDatabaseUseCase
import com.koflox.destinations.domain.usecase.NoSuitableDestinationException
import com.koflox.destinations.domain.usecase.ToleranceCalculator
import com.koflox.destinations.presentation.destinations.NavigationAction
import com.koflox.destinations.presentation.destinations.RideMapInternalState
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.mapper.DestinationUiMapper
import com.koflox.distance.DistanceCalculator
import com.koflox.graphics.curves.createCurvePoints
import com.koflox.graphics.primitives.Point
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

internal class DestinationDelegate(
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase,
    private val getDestinationInfoUseCase: GetDestinationInfoUseCase,
    private val getDistanceBoundsUseCase: GetDistanceBoundsUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val uiMapper: DestinationUiMapper,
    private val toleranceCalculator: ToleranceCalculator,
    private val application: Application,
    private val uiState: MutableStateFlow<RideMapInternalState>,
    private val scope: CoroutineScope,
) {

    companion object {
        private const val DESTINATION_RELOAD_THRESHOLD_KM = 50.0
        private const val BOUNDS_RECALCULATION_THRESHOLD_KM = 1.0
        private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }

    private val lastDestinationLoadLocation = AtomicReference<Location?>(null)
    private val lastBoundsCalculationLocation = AtomicReference<Location?>(null)

    fun startDestinationLoading(location: Location) {
        scope.launch {
            lastDestinationLoadLocation.set(location)
            initializeDatabaseUseCase.init(location).collect { event ->
                when (event) {
                    is DestinationLoadingEvent.Loading -> {
                        uiState.update { it.copy(isPreparingDestinations = true, areDestinationsReady = false) }
                    }
                    is DestinationLoadingEvent.Completed -> {
                        uiState.update { it.copy(isPreparingDestinations = false, areDestinationsReady = true) }
                        calculateDistanceBounds(location)
                    }
                    is DestinationLoadingEvent.Error -> {
                        uiState.update {
                            it.copy(isPreparingDestinations = false, error = application.getString(R.string.error_not_handled))
                        }
                    }
                }
            }
        }
    }

    suspend fun calculateDistanceBounds(location: Location) {
        uiState.update { it.copy(isCalculatingBounds = true) }
        getDistanceBoundsUseCase.getBounds(location)
            .onSuccess { bounds ->
                uiState.update { state ->
                    if (bounds != null) {
                        val routeDistance = if (state.routeDistanceKm == 0.0) {
                            (bounds.minKm + bounds.maxKm) / 4
                        } else {
                            state.routeDistanceKm.coerceIn(bounds.minKm, bounds.maxKm)
                        }
                        state.copy(
                            distanceBounds = bounds,
                            routeDistanceKm = routeDistance,
                            toleranceKm = toleranceCalculator.calculateKm(routeDistance),
                            isCalculatingBounds = false,
                        )
                    } else {
                        state.copy(distanceBounds = null, isCalculatingBounds = false)
                    }
                }
                lastBoundsCalculationLocation.set(location)
            }
            .onFailure {
                uiState.update { it.copy(isCalculatingBounds = false) }
            }
    }

    suspend fun findDestination(destinationId: String? = null) {
        val isRecovery = destinationId != null
        val isSessionActive = isRecovery || uiState.value.isSessionActive
        uiState.update {
            it.copy(isLoading = true, selectedDestination = null, otherValidDestinations = emptyList(), curvePoints = emptyList())
        }
        getUserLocationUseCase.getLocation()
            .onSuccess { location ->
                uiState.update { it.copy(userLocation = location) }
                if (isRecovery) {
                    getSelectedDestination(location, destinationId, isSessionActive = true, isSessionRecovery = true)
                } else {
                    selectRandomDestination(location)
                }
            }
            .onFailure { error ->
                uiState.update {
                    it.copy(
                        isLoading = false,
                        isSessionActive = isSessionActive,
                        error = if (isRecovery) null else application.getString(R.string.failed_to_get_location, error.message),
                    )
                }
            }
    }

    private suspend fun selectRandomDestination(location: Location) {
        handleDestinationsResult(
            result = getDestinationInfoUseCase.getRandomDestinations(
                userLocation = location,
                targetDistanceKm = uiState.value.routeDistanceKm,
            ),
            location = location,
            isSessionActive = false,
            isSessionRecovery = false,
        )
    }

    private suspend fun getSelectedDestination(
        location: Location,
        destinationId: String,
        isSessionActive: Boolean,
        isSessionRecovery: Boolean,
    ) {
        handleDestinationsResult(
            result = getDestinationInfoUseCase.getDestinations(userLocation = location, destinationId = destinationId),
            location = location,
            isSessionActive = isSessionActive,
            isSessionRecovery = isSessionRecovery,
        )
    }

    private suspend fun handleDestinationsResult(
        result: Result<Destinations>,
        location: Location,
        isSessionActive: Boolean,
        isSessionRecovery: Boolean,
    ) {
        result.onSuccess { destinations ->
            val uiModel = uiMapper.toUiModel(destinations, location)
            val curvePoints = computeCurvePoints(location, uiModel.selected)
            uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDestination = uiModel.selected,
                    otherValidDestinations = uiModel.otherValidDestinations,
                    curvePoints = curvePoints,
                    isSessionActive = isSessionActive,
                    routeDistanceKm = if (isSessionRecovery) uiModel.selected.distanceKm.roundToInt().toDouble() else it.routeDistanceKm,
                    toleranceKm = if (isSessionRecovery) {
                        toleranceCalculator.calculateKm(uiModel.selected.distanceKm.roundToInt().toDouble())
                    } else {
                        it.toleranceKm
                    },
                )
            }
        }.onFailure { error ->
            handleDestinationError(error)
        }
    }

    private fun handleDestinationError(error: Throwable) {
        val message = when (error) {
            is NoSuitableDestinationException -> application.getString(R.string.error_too_far_from_supported_area)
            else -> application.getString(R.string.error_not_handled)
        }
        uiState.update { it.copy(isLoading = false, error = message) }
    }

    fun updateRouteDistance(distanceKm: Double) {
        uiState.update {
            it.copy(routeDistanceKm = distanceKm, toleranceKm = toleranceCalculator.calculateKm(distanceKm))
        }
    }

    fun checkAndReloadDestinationsIfNeeded(newLocation: Location) {
        if (uiState.value.isPreparingDestinations) return
        val lastLoadLocation = lastDestinationLoadLocation.get()
        if (lastLoadLocation == null) {
            startDestinationLoading(newLocation)
            return
        }
        val distanceKm = distanceCalculator.calculateKm(
            lat1 = lastLoadLocation.latitude, lon1 = lastLoadLocation.longitude,
            lat2 = newLocation.latitude, lon2 = newLocation.longitude,
        )
        if (distanceKm >= DESTINATION_RELOAD_THRESHOLD_KM) {
            startDestinationLoading(newLocation)
        }
    }

    suspend fun checkAndRecalculateBoundsIfNeeded(newLocation: Location) {
        if (uiState.value.isCalculatingBounds) return
        val lastLocation = lastBoundsCalculationLocation.get() ?: return
        val distanceKm = distanceCalculator.calculateKm(
            lat1 = lastLocation.latitude, lon1 = lastLocation.longitude,
            lat2 = newLocation.latitude, lon2 = newLocation.longitude,
        )
        if (distanceKm >= BOUNDS_RECALCULATION_THRESHOLD_KM) {
            calculateDistanceBounds(newLocation)
        }
    }

    fun updateCurvePointsForLocation(newLocation: Location) {
        val destination = uiState.value.selectedDestination ?: return
        val updatedCurvePoints = computeCurvePoints(newLocation, destination)
        uiState.update { it.copy(curvePoints = updatedCurvePoints) }
    }

    fun openInGoogleMaps(destination: DestinationUiModel) {
        if (isGoogleMapsInstalled()) {
            val uri = "google.navigation:q=${destination.location.latitude},${destination.location.longitude}&mode=b".toUri()
            uiState.update { it.copy(navigationAction = NavigationAction.OpenGoogleMaps(uri)) }
        } else {
            uiState.update { it.copy(error = application.getString(R.string.error_google_maps_not_installed)) }
        }
    }

    fun showMarkerOptionsDialog() {
        uiState.update { it.copy(showSelectedMarkerOptionsDialog = true) }
    }

    fun dismissSelectedMarkerOptionsDialog() {
        uiState.update { it.copy(showSelectedMarkerOptionsDialog = false) }
    }

    private fun isGoogleMapsInstalled(): Boolean = try {
        application.packageManager.getPackageInfo(GOOGLE_MAPS_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun computeCurvePoints(userLocation: Location, destination: DestinationUiModel): List<LatLng> =
        createCurvePoints(
            start = Point(userLocation.latitude, userLocation.longitude),
            end = Point(destination.location.latitude, destination.location.longitude),
        ).map { LatLng(it.x, it.y) }
}
