package com.koflox.session.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val destinationId: String,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val startLatitude: Double,
    val startLongitude: Double,
    val startTimeMs: Long,
    val lastResumedTimeMs: Long,
    val endTimeMs: Long?,
    val elapsedTimeMs: Long,
    val traveledDistanceKm: Double,
    val averageSpeedKmh: Double,
    val topSpeedKmh: Double,
    val status: String,
)
