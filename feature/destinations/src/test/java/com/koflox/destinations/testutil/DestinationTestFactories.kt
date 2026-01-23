package com.koflox.destinations.testutil

import com.koflox.destinations.data.source.asset.model.DestinationAsset
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.destinations.domain.model.Destination

fun createDestination(
    id: String = "",
    title: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
) = Destination(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
)

fun createDestinationLocal(
    id: String = "",
    title: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
) = DestinationLocal(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
)

fun createDestinationAsset(
    id: String = "",
    title: String = "",
    latitude: Double = 0.0,
    longitude: Double = 0.0,
) = DestinationAsset(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
)
