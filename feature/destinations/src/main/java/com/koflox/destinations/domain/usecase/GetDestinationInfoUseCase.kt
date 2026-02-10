package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.math.abs

class NoSuitableDestinationException : Exception("No suitable destination found")

interface GetDestinationInfoUseCase {
    suspend fun getRandomDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
    ): Result<Destinations>

    suspend fun getDestinations(
        userLocation: Location,
        destinationId: String,
    ): Result<Destinations>
}

internal class GetDestinationInfoUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val repository: DestinationRepository,
    private val getNearbyDestinationsUseCase: GetNearbyDestinationsUseCase,
    private val distanceCalculator: DistanceCalculator,
    private val toleranceCalculator: ToleranceCalculator,
) : GetDestinationInfoUseCase {

    companion object {
        private const val MAX_SEARCH_RADIUS_KM = 150.0
    }

    override suspend fun getRandomDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
    ): Result<Destinations> = withContext(dispatcherDefault) {
        getDestinations(
            userLocation = userLocation,
            targetDistanceKm = targetDistanceKm,
            getMainDestination = { destinations ->
                destinations.random()
            },
        )
    }

    override suspend fun getDestinations(
        userLocation: Location,
        destinationId: String,
    ): Result<Destinations> {
        val destination = repository.getDestinationById(destinationId).getOrNull() ?: throw NoSuitableDestinationException()
        val targetDistanceKm = distanceCalculator.calculateKm(
            lat1 = userLocation.latitude,
            lon1 = userLocation.longitude,
            lat2 = destination.latitude,
            lon2 = destination.longitude,
        )
        return getDestinations(
            userLocation = userLocation,
            targetDistanceKm = targetDistanceKm,
            getMainDestination = { _ ->
                destination
            },
        )
    }

    private suspend fun getDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        getMainDestination: (List<Destination>) -> Destination,
    ): Result<Destinations> {
        val toleranceKm = toleranceCalculator.calculateKm(targetDistanceKm)
        return getNearbyDestinationsUseCase.getDestinations(
            userLocation = userLocation,
            minDistanceKm = targetDistanceKm - toleranceKm,
            maxDistanceKm = targetDistanceKm + toleranceKm,
        ).mapCatching { destinations ->
            if (destinations.isNotEmpty()) {
                val mainDestination = getMainDestination(destinations)
                Destinations(
                    mainDestination = mainDestination,
                    otherValidDestinations = destinations - mainDestination,
                )
            } else {
                val nearest = findNearestDestination(userLocation, targetDistanceKm)
                    ?: throw NoSuitableDestinationException()
                Destinations(
                    mainDestination = nearest,
                    otherValidDestinations = emptyList(),
                )
            }
        }
    }

    private suspend fun findNearestDestination(
        userLocation: Location,
        targetDistanceKm: Double,
    ): Destination? = getNearbyDestinationsUseCase.getDestinations(
        userLocation = userLocation,
        minDistanceKm = 0.0,
        maxDistanceKm = MAX_SEARCH_RADIUS_KM,
    ).getOrNull()?.minByOrNull { destination ->
        abs(
            distanceCalculator.calculateKm(
                lat1 = userLocation.latitude,
                lon1 = userLocation.longitude,
                lat2 = destination.latitude,
                lon2 = destination.longitude,
            ) - targetDistanceKm,
        )
    }
}
