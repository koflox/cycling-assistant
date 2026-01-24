package com.koflox.location.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null,
)
