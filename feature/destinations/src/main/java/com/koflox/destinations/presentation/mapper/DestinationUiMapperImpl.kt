package com.koflox.destinations.presentation.mapper

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.domain.util.DistanceCalculator
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.destinations.model.DestinationsUiModel
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DestinationUiMapperImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val distanceCalculator: DistanceCalculator,
) : DestinationUiMapper {
    override suspend fun toUiModel(
        destinations: Destinations,
        userLocation: Location,
    ): DestinationsUiModel = withContext(dispatcherDefault) {
        val validDestinationUiModels = destinations.otherValidDestinations.map { d ->
            val distance = getDistance(userLocation, d)
            mapToModel(
                d = d,
                distance = distance,
                isMain = false,
            )
        }
        val targetDestinationUiModel = mapToModel(
            d = destinations.randomizedDestination,
            distance = getDistance(userLocation, destinations.randomizedDestination),
            isMain = true,
        )
        DestinationsUiModel(
            selected = targetDestinationUiModel,
            otherValidDestinations = validDestinationUiModels,
        )
    }

    private fun mapToModel(
        d: Destination,
        distance: Double,
        isMain: Boolean,
    ): DestinationUiModel = DestinationUiModel(
        id = d.id,
        title = d.title,
        location = Location(
            latitude = d.latitude,
            longitude = d.longitude,
        ),
        distanceKm = distance,
        isMain = isMain,
    )

    private suspend fun getDistance(userLocation: Location, destination: Destination): Double =
        distanceCalculator.calculate(
            lat1 = userLocation.latitude,
            lon1 = userLocation.longitude,
            lat2 = destination.latitude,
            lon2 = destination.longitude,
        )
}
