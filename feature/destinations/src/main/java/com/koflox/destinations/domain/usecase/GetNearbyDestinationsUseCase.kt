package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.repository.DestinationsRepository
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlin.math.cos

/**
 * Fetches destinations within a distance range using two-stage filtering:
 *
 * 1. SQL bounding box (rectangle) — fast pre-filter via DB query
 * 2. Exact distance calculation — post-filter removing false positives from box corners
 *
 * ```
 * maxLat ┌────────────┐
 *        │  ╭──────╮  │
 *        │ ╱        ╲ │  <- corners are inside the SQL box
 *        │╱          ╲│     but beyond maxDistanceKm
 * userLat│     ●      │
 *        │╲          ╱│
 *        │ ╲        ╱ │
 *        │  ╰──────╯  │
 * minLat └────────────┘
 *      minLon       maxLon
 * ```
 */
internal interface GetNearbyDestinationsUseCase {
    suspend fun getDestinations(
        userLocation: Location,
        minDistanceKm: Double,
        maxDistanceKm: Double,
    ): Result<List<Destination>>
}

internal class GetNearbyDestinationsUseCaseImpl(
    private val repository: DestinationsRepository,
    private val distanceCalculator: DistanceCalculator,
) : GetNearbyDestinationsUseCase {

    companion object {
        private const val KM_PER_DEGREE = 111.0
    }

    override suspend fun getDestinations(
        userLocation: Location,
        minDistanceKm: Double,
        maxDistanceKm: Double,
    ): Result<List<Destination>> {
        val deltaLat = maxDistanceKm / KM_PER_DEGREE
        val deltaLon = maxDistanceKm / (KM_PER_DEGREE * cos(Math.toRadians(userLocation.latitude)))
        return repository.getDestinationsInArea(
            minLat = userLocation.latitude - deltaLat,
            maxLat = userLocation.latitude + deltaLat,
            minLon = userLocation.longitude - deltaLon,
            maxLon = userLocation.longitude + deltaLon,
        ).map { destinations ->
            destinations.filter { destination ->
                val distance = distanceCalculator.calculateKm(
                    lat1 = userLocation.latitude,
                    lon1 = userLocation.longitude,
                    lat2 = destination.latitude,
                    lon2 = destination.longitude,
                )
                distance in minDistanceKm..maxDistanceKm
            }
        }
    }
}
