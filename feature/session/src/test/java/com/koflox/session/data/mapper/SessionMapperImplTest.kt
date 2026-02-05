package com.koflox.session.data.mapper

import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createSessionEntity
import com.koflox.session.testutil.createSessionWithTrackPoints
import com.koflox.session.testutil.createTrackPoint
import com.koflox.session.testutil.createTrackPointEntity
import com.koflox.testing.coroutine.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionMapperImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_ID = "dest-456"
        private const val DESTINATION_NAME = "Test Destination"
        private const val DESTINATION_LAT = 52.52
        private const val DESTINATION_LONG = 13.405
        private const val START_LAT = 52.50
        private const val START_LONG = 13.40
        private const val START_TIME_MS = 1000000L
        private const val LAST_RESUMED_TIME_MS = 1000000L
        private const val END_TIME_MS = 2000000L
        private const val ELAPSED_TIME_MS = 900000L
        private const val TRAVELED_DISTANCE_KM = 5.5
        private const val AVERAGE_SPEED_KMH = 22.0
        private const val TOP_SPEED_KMH = 35.0
        private const val TRACK_POINT_ID = "tp-123"
        private const val TRACK_POINT_LAT = 52.51
        private const val TRACK_POINT_LONG = 13.41
        private const val TRACK_POINT_TIMESTAMP = 1500000L
        private const val TRACK_POINT_SPEED = 25.0
        private const val TRACK_POINT_ACCURACY = 8.5f
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mapper: SessionMapperImpl

    @Before
    fun setup() {
        mapper = SessionMapperImpl(mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `toEntity maps session id correctly`() = runTest {
        val session = createTestSession()

        val entity = mapper.toEntity(session)

        assertEquals(SESSION_ID, entity.id)
    }

    @Test
    fun `toEntity maps destination fields correctly`() = runTest {
        val session = createTestSession()

        val entity = mapper.toEntity(session)

        assertEquals(DESTINATION_ID, entity.destinationId)
        assertEquals(DESTINATION_NAME, entity.destinationName)
        assertEquals(DESTINATION_LAT, entity.destinationLatitude, 0.0)
        assertEquals(DESTINATION_LONG, entity.destinationLongitude, 0.0)
    }

    @Test
    fun `toEntity maps start location correctly`() = runTest {
        val session = createTestSession()

        val entity = mapper.toEntity(session)

        assertEquals(START_LAT, entity.startLatitude, 0.0)
        assertEquals(START_LONG, entity.startLongitude, 0.0)
    }

    @Test
    fun `toEntity maps time fields correctly`() = runTest {
        val session = createTestSession()

        val entity = mapper.toEntity(session)

        assertEquals(START_TIME_MS, entity.startTimeMs)
        assertEquals(LAST_RESUMED_TIME_MS, entity.lastResumedTimeMs)
        assertEquals(END_TIME_MS, entity.endTimeMs)
        assertEquals(ELAPSED_TIME_MS, entity.elapsedTimeMs)
    }

    @Test
    fun `toEntity maps statistics correctly`() = runTest {
        val session = createTestSession()

        val entity = mapper.toEntity(session)

        assertEquals(TRAVELED_DISTANCE_KM, entity.traveledDistanceKm, 0.0)
        assertEquals(AVERAGE_SPEED_KMH, entity.averageSpeedKmh, 0.0)
        assertEquals(TOP_SPEED_KMH, entity.topSpeedKmh, 0.0)
    }

    @Test
    fun `toEntity maps status to string`() = runTest {
        val session = createTestSession(status = SessionStatus.RUNNING)

        val entity = mapper.toEntity(session)

        assertEquals("RUNNING", entity.status)
    }

    @Test
    fun `toEntity maps paused status correctly`() = runTest {
        val session = createTestSession(status = SessionStatus.PAUSED)

        val entity = mapper.toEntity(session)

        assertEquals("PAUSED", entity.status)
    }

    @Test
    fun `toEntity maps completed status correctly`() = runTest {
        val session = createTestSession(status = SessionStatus.COMPLETED)

        val entity = mapper.toEntity(session)

        assertEquals("COMPLETED", entity.status)
    }

    @Test
    fun `toTrackPointEntities maps session id to all entities`() = runTest {
        val trackPoints = listOf(
            createTestTrackPoint(),
            createTrackPoint(latitude = 52.52, longitude = 13.42),
        )

        val entities = mapper.toTrackPointEntities(SESSION_ID, trackPoints)

        assertEquals(2, entities.size)
        entities.forEach { assertEquals(SESSION_ID, it.sessionId) }
    }

    @Test
    fun `toTrackPointEntities maps coordinates correctly`() = runTest {
        val trackPoints = listOf(createTestTrackPoint())

        val entities = mapper.toTrackPointEntities(SESSION_ID, trackPoints)

        assertEquals(TRACK_POINT_LAT, entities[0].latitude, 0.0)
        assertEquals(TRACK_POINT_LONG, entities[0].longitude, 0.0)
    }

    @Test
    fun `toTrackPointEntities maps timestamp and speed correctly`() = runTest {
        val trackPoints = listOf(createTestTrackPoint())

        val entities = mapper.toTrackPointEntities(SESSION_ID, trackPoints)

        assertEquals(TRACK_POINT_TIMESTAMP, entities[0].timestampMs)
        assertEquals(TRACK_POINT_SPEED, entities[0].speedKmh, 0.0)
    }

    @Test
    fun `toTrackPointEntities maps id and new fields correctly`() = runTest {
        val trackPoints = listOf(createTestTrackPoint())

        val entities = mapper.toTrackPointEntities(SESSION_ID, trackPoints)

        assertEquals(TRACK_POINT_ID, entities[0].id)
        assertTrue(entities[0].isSegmentStart)
        assertEquals(TRACK_POINT_ACCURACY, entities[0].accuracyMeters)
    }

    @Test
    fun `toTrackPointEntities returns empty list for empty input`() = runTest {
        val entities = mapper.toTrackPointEntities(SESSION_ID, emptyList())

        assertEquals(0, entities.size)
    }

    @Test
    fun `toTrackPointEntities preserves order`() = runTest {
        val trackPoints = listOf(
            createTrackPoint(latitude = 1.0),
            createTrackPoint(latitude = 2.0),
            createTrackPoint(latitude = 3.0),
        )

        val entities = mapper.toTrackPointEntities(SESSION_ID, trackPoints)

        assertEquals(1.0, entities[0].latitude, 0.0)
        assertEquals(2.0, entities[1].latitude, 0.0)
        assertEquals(3.0, entities[2].latitude, 0.0)
    }

    @Test
    fun `toDomain maps session id correctly`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()

        val session = mapper.toDomain(sessionWithTrackPoints)

        assertEquals(SESSION_ID, session.id)
    }

    @Test
    fun `toDomain maps destination fields correctly`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()

        val session = mapper.toDomain(sessionWithTrackPoints)

        assertEquals(DESTINATION_ID, session.destinationId)
        assertEquals(DESTINATION_NAME, session.destinationName)
        assertEquals(DESTINATION_LAT, session.destinationLatitude, 0.0)
        assertEquals(DESTINATION_LONG, session.destinationLongitude, 0.0)
    }

    @Test
    fun `toDomain maps status from string`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints(status = "RUNNING")

        val session = mapper.toDomain(sessionWithTrackPoints)

        assertEquals(SessionStatus.RUNNING, session.status)
    }

    @Test
    fun `toDomain maps paused status from string`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints(status = "PAUSED")

        val session = mapper.toDomain(sessionWithTrackPoints)

        assertEquals(SessionStatus.PAUSED, session.status)
    }

    @Test
    fun `toDomain maps track points correctly`() = runTest {
        val trackPointEntities = listOf(
            createTestTrackPointEntity(),
            createTrackPointEntity(sessionId = SESSION_ID, latitude = 52.52),
        )
        val sessionWithTrackPoints = createTestSessionWithTrackPoints(trackPoints = trackPointEntities)

        val session = mapper.toDomain(sessionWithTrackPoints)

        assertEquals(2, session.trackPoints.size)
        assertEquals(TRACK_POINT_LAT, session.trackPoints[0].latitude, 0.0)
        assertEquals(52.52, session.trackPoints[1].latitude, 0.0)
    }

    @Test
    fun `toDomain maps track point fields correctly`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()

        val session = mapper.toDomain(sessionWithTrackPoints)

        val trackPoint = session.trackPoints[0]
        assertEquals(TRACK_POINT_LAT, trackPoint.latitude, 0.0)
        assertEquals(TRACK_POINT_LONG, trackPoint.longitude, 0.0)
        assertEquals(TRACK_POINT_TIMESTAMP, trackPoint.timestampMs)
        assertEquals(TRACK_POINT_SPEED, trackPoint.speedKmh, 0.0)
    }

    @Test
    fun `toDomain maps track point id and new fields correctly`() = runTest {
        val sessionWithTrackPoints = createTestSessionWithTrackPoints()

        val session = mapper.toDomain(sessionWithTrackPoints)

        val trackPoint = session.trackPoints[0]
        assertEquals(TRACK_POINT_ID, trackPoint.id)
        assertTrue(trackPoint.isSegmentStart)
        assertEquals(TRACK_POINT_ACCURACY, trackPoint.accuracyMeters)
    }

    @Test
    fun `toDomainList maps multiple sessions`() = runTest {
        val sessions = listOf(
            createTestSessionWithTrackPoints(sessionId = "session-1"),
            createTestSessionWithTrackPoints(sessionId = "session-2"),
        )

        val domainSessions = mapper.toDomainList(sessions)

        assertEquals(2, domainSessions.size)
        assertEquals("session-1", domainSessions[0].id)
        assertEquals("session-2", domainSessions[1].id)
    }

    @Test
    fun `toDomainList returns empty list for empty input`() = runTest {
        val domainSessions = mapper.toDomainList(emptyList())

        assertEquals(0, domainSessions.size)
    }

    @Test
    fun `toDomainList preserves order`() = runTest {
        val sessions = listOf(
            createTestSessionWithTrackPoints(sessionId = "first"),
            createTestSessionWithTrackPoints(sessionId = "second"),
            createTestSessionWithTrackPoints(sessionId = "third"),
        )

        val domainSessions = mapper.toDomainList(sessions)

        assertEquals("first", domainSessions[0].id)
        assertEquals("second", domainSessions[1].id)
        assertEquals("third", domainSessions[2].id)
    }

    private fun createTestSession(
        id: String = SESSION_ID,
        status: SessionStatus = SessionStatus.COMPLETED,
    ) = createSession(
        id = id,
        destinationId = DESTINATION_ID,
        destinationName = DESTINATION_NAME,
        destinationLatitude = DESTINATION_LAT,
        destinationLongitude = DESTINATION_LONG,
        startLatitude = START_LAT,
        startLongitude = START_LONG,
        startTimeMs = START_TIME_MS,
        lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        endTimeMs = END_TIME_MS,
        elapsedTimeMs = ELAPSED_TIME_MS,
        traveledDistanceKm = TRAVELED_DISTANCE_KM,
        averageSpeedKmh = AVERAGE_SPEED_KMH,
        topSpeedKmh = TOP_SPEED_KMH,
        status = status,
        trackPoints = listOf(createTestTrackPoint()),
    )

    private fun createTestTrackPoint(
        id: String = TRACK_POINT_ID,
        latitude: Double = TRACK_POINT_LAT,
        longitude: Double = TRACK_POINT_LONG,
        timestampMs: Long = TRACK_POINT_TIMESTAMP,
        speedKmh: Double = TRACK_POINT_SPEED,
        isSegmentStart: Boolean = true,
        accuracyMeters: Float? = TRACK_POINT_ACCURACY,
    ) = createTrackPoint(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestampMs = timestampMs,
        speedKmh = speedKmh,
        isSegmentStart = isSegmentStart,
        accuracyMeters = accuracyMeters,
    )

    private fun createTestSessionEntity(
        id: String = SESSION_ID,
        status: String = "COMPLETED",
    ) = createSessionEntity(
        id = id,
        destinationId = DESTINATION_ID,
        destinationName = DESTINATION_NAME,
        destinationLatitude = DESTINATION_LAT,
        destinationLongitude = DESTINATION_LONG,
        startLatitude = START_LAT,
        startLongitude = START_LONG,
        startTimeMs = START_TIME_MS,
        lastResumedTimeMs = LAST_RESUMED_TIME_MS,
        endTimeMs = END_TIME_MS,
        elapsedTimeMs = ELAPSED_TIME_MS,
        traveledDistanceKm = TRAVELED_DISTANCE_KM,
        averageSpeedKmh = AVERAGE_SPEED_KMH,
        topSpeedKmh = TOP_SPEED_KMH,
        status = status,
    )

    private fun createTestTrackPointEntity(
        id: String = TRACK_POINT_ID,
        latitude: Double = TRACK_POINT_LAT,
        longitude: Double = TRACK_POINT_LONG,
        isSegmentStart: Boolean = true,
        accuracyMeters: Float? = TRACK_POINT_ACCURACY,
    ) = createTrackPointEntity(
        id = id,
        sessionId = SESSION_ID,
        latitude = latitude,
        longitude = longitude,
        timestampMs = TRACK_POINT_TIMESTAMP,
        speedKmh = TRACK_POINT_SPEED,
        isSegmentStart = isSegmentStart,
        accuracyMeters = accuracyMeters,
    )

    private fun createTestSessionWithTrackPoints(
        sessionId: String = SESSION_ID,
        status: String = "COMPLETED",
        trackPoints: List<com.koflox.session.data.source.local.entity.TrackPointEntity> = listOf(createTestTrackPointEntity()),
    ) = createSessionWithTrackPoints(
        session = createTestSessionEntity(id = sessionId, status = status),
        trackPoints = trackPoints,
    )
}
