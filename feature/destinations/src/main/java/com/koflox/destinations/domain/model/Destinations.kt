package com.koflox.destinations.domain.model

data class Destinations(
    val randomizedDestination: Destination,
    val otherValidDestinations: List<Destination>,
)
