package com.koflox.destinations.data.source.asset.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DestinationAsset(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("lat")
    val latitude: Double,
    @SerialName("long")
    val longitude: Double,
)
