package com.koflox.destinations.data.source.asset.model

internal data class DestinationFileMetadata(
    val fileName: String,
    val city: String,
    val country: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val tier: Int,
)
