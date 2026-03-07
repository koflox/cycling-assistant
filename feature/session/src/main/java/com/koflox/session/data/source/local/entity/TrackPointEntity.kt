package com.koflox.session.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "track_points",
    primaryKeys = ["sessionId", "pointIndex"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TrackPointEntity(
    val sessionId: String,
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val timestampMs: Long,
    val speedKmh: Double,
    val altitudeMeters: Double?,
    val isSegmentStart: Boolean,
    val accuracyMeters: Float?,
    val powerWatts: Int? = null,
)
