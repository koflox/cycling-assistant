package com.koflox.destinations.data.source.asset

import android.content.Context
import android.util.Log
import com.koflox.destinations.data.source.asset.model.DestinationFileMetadata
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DestinationFileResolverImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val context: Context,
    private val distanceCalculator: DistanceCalculator,
) : DestinationFileResolver {

    companion object {
        private const val DESTINATIONS_PREFIX = "destinations_"
        private const val MAX_DISTANCE_KM = 100.0
        private val FILE_NAME_REGEX = Regex(
            """destinations_([a-z]+)_([a-z]+)_(-?\d+\.?\d*)_(-?\d+\.?\d*)_tier(\d+)\.json"""
        )
    }

    override suspend fun getFilesWithinRadius(userLocation: Location): List<DestinationFileMetadata> =
        withContext(dispatcherIo) {
            scanDestinationFiles()
                .filter { file ->
                    val distanceKm = distanceCalculator.calculateKm(
                        lat1 = userLocation.latitude,
                        lon1 = userLocation.longitude,
                        lat2 = file.centerLatitude,
                        lon2 = file.centerLongitude,
                    )
                    distanceKm <= MAX_DISTANCE_KM
                }
                .sortedWith(compareBy({ it.city }, { it.tier }))
                .also {
                    Log.d("Logos", it.toString())
                }
        }

    private fun scanDestinationFiles(): List<DestinationFileMetadata> {
        return context.assets.list("")
            ?.filter { it.startsWith(DESTINATIONS_PREFIX) && it.endsWith(".json") }
            ?.mapNotNull { parseFileName(it) }
            ?: emptyList()
    }

    private fun parseFileName(fileName: String): DestinationFileMetadata? =
        FILE_NAME_REGEX.matchEntire(fileName)?.let { matchResult ->
            val groups = matchResult.groupValues
            val latitude = groups[3].toDoubleOrNull()
            val longitude = groups[4].toDoubleOrNull()
            val tier = groups[5].toIntOrNull()
            if (latitude != null && longitude != null && tier != null) {
                DestinationFileMetadata(
                    fileName = fileName,
                    city = groups[1],
                    country = groups[2],
                    centerLatitude = latitude,
                    centerLongitude = longitude,
                    tier = tier,
                )
            } else {
                null
            }
        }
}
