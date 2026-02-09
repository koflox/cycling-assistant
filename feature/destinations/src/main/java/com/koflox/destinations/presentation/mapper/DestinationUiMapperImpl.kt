package com.koflox.destinations.presentation.mapper

import android.content.Context
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinations.presentation.destinations.model.DestinationsUiModel
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale

internal class DestinationUiMapperImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val distanceCalculator: DistanceCalculator,
    private val context: Context,
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
            d = destinations.mainDestination,
            distance = getDistance(userLocation, destinations.mainDestination),
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
        distanceFormatted = String.format(
            Locale.getDefault(),
            context.getString(R.string.distance_to_dest_desc),
            distance,
        ),
        isMain = isMain,
    )

    private fun getDistance(userLocation: Location, destination: Destination): Double =
        distanceCalculator.calculateKm(
            lat1 = userLocation.latitude,
            lon1 = userLocation.longitude,
            lat2 = destination.latitude,
            lon2 = destination.longitude,
        )
}
