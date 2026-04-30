package com.koflox.session.data.source.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithTrackPoints(
    @Embedded
    val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val trackPoints: List<TrackPointEntity>,
)
