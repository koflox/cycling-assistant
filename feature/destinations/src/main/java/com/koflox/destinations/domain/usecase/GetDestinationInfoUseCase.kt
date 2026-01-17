package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class NoSuitableDestinationException : Exception("No suitable destination found")

interface GetDestinationInfoUseCase {
    suspend fun getRandomDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        toleranceKm: Double,
    ): Result<Destinations>

    suspend fun getDestinations(
        userLocation: Location,
        destinationId: String,
        toleranceKm: Double,
    ): Result<Destinations>
}

internal class GetDestinationInfoUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val repository: DestinationRepository,
    private val distanceCalculator: DistanceCalculator,
) : GetDestinationInfoUseCase {
    override suspend fun getRandomDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        toleranceKm: Double,
    ): Result<Destinations> = withContext(dispatcherDefault) {
        getDestinations(
            userLocation = userLocation,
            targetDistanceKm = targetDistanceKm,
            toleranceKm = toleranceKm,
            getMainDestination = { destinations ->
                destinations.random()
            },
        )
    }

    private suspend fun getDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        toleranceKm: Double,
        getMainDestination: (List<Destination>) -> Destination,
    ): Result<Destinations> = repository.getAllDestinations()
        .mapCatching { destinations ->
            val validDestinations = destinations.filter { destination ->
                val distance = distanceCalculator.calculateKm(
                    lat1 = userLocation.latitude,
                    lon1 = userLocation.longitude,
                    lat2 = destination.latitude,
                    lon2 = destination.longitude,
                )
                distance in targetDistanceKm - toleranceKm..targetDistanceKm + toleranceKm
            }
            if (validDestinations.isEmpty()) throw NoSuitableDestinationException()
            val mainDestination = getMainDestination(validDestinations)
            Destinations(
                mainDestination = mainDestination,
                otherValidDestinations = validDestinations - mainDestination,
            )
        }

    override suspend fun getDestinations(
        userLocation: Location,
        destinationId: String,
        toleranceKm: Double,
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
            toleranceKm = toleranceKm,
            getMainDestination = { _ ->
                destination
            },
        )
    }

}
