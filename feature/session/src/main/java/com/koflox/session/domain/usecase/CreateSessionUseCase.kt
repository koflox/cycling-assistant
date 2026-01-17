package com.koflox.session.domain.usecase

import com.koflox.concurrent.suspendRunCatching
import com.koflox.id.IdGenerator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository

interface CreateSessionUseCase {
    suspend fun create(params: CreateSessionParams): Result<String>
}

data class CreateSessionParams(
    val destinationId: String,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val startLatitude: Double,
    val startLongitude: Double,
)

internal class CreateSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val idGenerator: IdGenerator,
) : CreateSessionUseCase {

    override suspend fun create(params: CreateSessionParams): Result<String> = suspendRunCatching {
        val currentTimeMs = System.currentTimeMillis()
        val session = Session(
            id = idGenerator.generate(),
            destinationId = params.destinationId,
            destinationName = params.destinationName,
            destinationLatitude = params.destinationLatitude,
            destinationLongitude = params.destinationLongitude,
            startLatitude = params.startLatitude,
            startLongitude = params.startLongitude,
            startTimeMs = currentTimeMs,
            lastResumedTimeMs = currentTimeMs,
            endTimeMs = null,
            elapsedTimeMs = 0L,
            traveledDistanceKm = 0.0,
            averageSpeedKmh = 0.0,
            topSpeedKmh = 0.0,
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                TrackPoint(
                    latitude = params.startLatitude,
                    longitude = params.startLongitude,
                    timestampMs = currentTimeMs,
                    speedKmh = 0.0,
                ),
            ),
        )
        sessionRepository.saveSession(session).getOrThrow()
        session.id
    }
}
