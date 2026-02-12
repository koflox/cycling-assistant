package com.koflox.session.domain.usecase

import com.koflox.id.IdGenerator
import com.koflox.location.error.LocationUnavailableException
import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.model.Location
import com.koflox.location.validator.LocationValidator
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.domain.model.TrackPoint
import com.koflox.session.domain.repository.SessionRepository
import kotlinx.coroutines.delay

interface CreateSessionUseCase {
    suspend fun create(params: CreateSessionParams): Result<String>
}

sealed class CreateSessionParams {
    data object FreeRoam : CreateSessionParams()
    data class Destination(
        val destinationId: String,
        val destinationName: String,
        val destinationLatitude: Double,
        val destinationLongitude: Double,
    ) : CreateSessionParams()
}

internal class CreateSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val idGenerator: IdGenerator,
    private val locationDataSource: LocationDataSource,
    private val locationValidator: LocationValidator,
) : CreateSessionUseCase {

    companion object {
        private const val LOCATION_MAX_RETRIES = 3
        private const val LOCATION_RETRY_DELAY_MS = 2000L
    }

    override suspend fun create(params: CreateSessionParams): Result<String> {
        val startLocation = getValidLocation()
            ?: return Result.failure(LocationUnavailableException())
        val currentTimeMs = System.currentTimeMillis()
        val destination = params as? CreateSessionParams.Destination
        val session = Session(
            id = idGenerator.generate(),
            destinationId = destination?.destinationId,
            destinationName = destination?.destinationName,
            destinationLatitude = destination?.destinationLatitude,
            destinationLongitude = destination?.destinationLongitude,
            startLatitude = startLocation.latitude,
            startLongitude = startLocation.longitude,
            startTimeMs = currentTimeMs,
            lastResumedTimeMs = currentTimeMs,
            endTimeMs = null,
            elapsedTimeMs = 0L,
            traveledDistanceKm = 0.0,
            averageSpeedKmh = 0.0,
            topSpeedKmh = 0.0,
            totalAltitudeGainMeters = 0.0,
            status = SessionStatus.RUNNING,
            trackPoints = listOf(
                TrackPoint(
                    id = idGenerator.generate(),
                    latitude = startLocation.latitude,
                    longitude = startLocation.longitude,
                    timestampMs = currentTimeMs,
                    speedKmh = 0.0,
                    altitudeMeters = startLocation.altitudeMeters,
                    isSegmentStart = true,
                    accuracyMeters = startLocation.accuracyMeters,
                ),
            ),
        )
        return sessionRepository.saveSession(session).map { session.id }
    }

    private suspend fun getValidLocation(): Location? {
        var bestLocation: Location? = null
        repeat(LOCATION_MAX_RETRIES) { attempt ->
            locationDataSource.getCurrentLocation()
                .onSuccess { location ->
                    bestLocation = location
                    if (locationValidator.isAccuracyValid(location)) return location
                }
            if (attempt < LOCATION_MAX_RETRIES - 1) delay(LOCATION_RETRY_DELAY_MS)
        }
        return bestLocation
    }
}
