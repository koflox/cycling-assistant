package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.DistanceBounds
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

interface GetDistanceBoundsUseCase {
    suspend fun getBounds(userLocation: Location): Result<DistanceBounds?>
}

internal class GetDistanceBoundsUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase,
    private val distanceCalculator: DistanceCalculator,
) : GetDistanceBoundsUseCase {

    companion object {
        private const val MAX_NEARBY_RADIUS_KM = 150.0
        private const val MIN_NEARBY_RADIUS_KM = 0.0
        private const val MIN_BOUND_DISTANCE_KM = 1.0
    }

    override suspend fun getBounds(userLocation: Location): Result<DistanceBounds?> = withContext(dispatcherDefault) {
        getNearbyDestinationsUseCase.getDestinations(
            userLocation = userLocation,
            minDistanceKm = MIN_NEARBY_RADIUS_KM,
            maxDistanceKm = MAX_NEARBY_RADIUS_KM,
        ).map { destinations ->
            if (destinations.isEmpty()) return@map null
            val distances = destinations.map { destination ->
                distanceCalculator.calculateKm(
                    lat1 = userLocation.latitude,
                    lon1 = userLocation.longitude,
                    lat2 = destination.latitude,
                    lon2 = destination.longitude,
                )
            }
            DistanceBounds(
                minKm = floor(distances.min()).coerceAtLeast(MIN_BOUND_DISTANCE_KM),
                maxKm = ceil(distances.max()).coerceAtLeast(MIN_BOUND_DISTANCE_KM),
            )
        }
    }
}
