package com.koflox.session.data.mapper

import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.TrackPoint

internal interface SessionMapper {

    suspend fun toEntity(session: Session): SessionEntity

    suspend fun toTrackPointEntities(sessionId: String, trackPoints: List<TrackPoint>): List<TrackPointEntity>

    suspend fun toDomain(entity: SessionEntity, trackPoints: List<TrackPointEntity>): Session

    suspend fun toDomainList(entities: List<SessionEntity>): List<Session>
}
