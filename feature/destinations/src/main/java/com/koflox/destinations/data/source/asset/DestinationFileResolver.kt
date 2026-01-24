package com.koflox.destinations.data.source.asset

import com.koflox.destinations.data.source.asset.model.DestinationFileMetadata
import com.koflox.location.model.Location

internal interface DestinationFileResolver {
    suspend fun getFilesWithinRadius(userLocation: Location): List<DestinationFileMetadata>
}
