package com.koflox.destinations.domain.model

data class Destinations(
    val mainDestination: Destination,
    val otherValidDestinations: List<Destination>,
)
