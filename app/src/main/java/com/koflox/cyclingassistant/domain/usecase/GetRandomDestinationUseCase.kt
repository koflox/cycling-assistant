package com.koflox.cyclingassistant.domain.usecase

import com.koflox.cyclingassistant.domain.model.Destination
import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.domain.repository.DestinationRepository
import com.koflox.cyclingassistant.domain.util.DistanceCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

data class Destinations(
    val randomizedDestination: Destination,
    val otherValidDestinations: List<Destination>,
)

interface GetRandomDestinationUseCase {
    suspend fun getDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        toleranceKm: Double,
        areAllValidDestinationsIncluded: Boolean,
    ): Result<Destinations>
}

internal class GetRandomDestinationUseCaseImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val repository: DestinationRepository,
    private val distanceCalculator: DistanceCalculator,
) : GetRandomDestinationUseCase {
    override suspend fun getDestinations(
        userLocation: Location,
        targetDistanceKm: Double,
        toleranceKm: Double,
        areAllValidDestinationsIncluded: Boolean,
    ): Result<Destinations> = withContext(dispatcherDefault) {
        repository.getAllDestinations()
            .mapCatching { destinations ->
                val validDestinations = destinations.filter { destination ->
                    val distance = distanceCalculator.calculate(
                        lat1 = userLocation.latitude,
                        lon1 = userLocation.longitude,
                        lat2 = destination.latitude,
                        lon2 = destination.longitude,
                    )
                    distance in targetDistanceKm - toleranceKm..targetDistanceKm + toleranceKm
                }
                if (validDestinations.isEmpty()) throw NoSuitableDestinationException()
                val randomizedDestination = validDestinations.random()
                Destinations(
                    randomizedDestination = randomizedDestination,
                    otherValidDestinations = validDestinations - randomizedDestination
                )
            }
    }

    companion object {
        const val DEFAULT_TOLERANCE_KM = 4.0
    }
}

class NoSuitableDestinationException : Exception("No suitable destination found")
