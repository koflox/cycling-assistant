package com.koflox.session.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_points",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class TrackPointEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val timestampMs: Long,
    val speedKmh: Double,
    val altitudeMeters: Double?,
    val isSegmentStart: Boolean,
    val accuracyMeters: Float?,
)
