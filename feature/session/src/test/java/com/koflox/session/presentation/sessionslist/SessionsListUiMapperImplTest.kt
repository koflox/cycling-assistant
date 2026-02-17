package com.koflox.session.presentation.sessionslist

import com.koflox.designsystem.testutil.mockLocalizedContextProvider
import com.koflox.session.domain.model.SessionStatus
import com.koflox.session.testutil.createSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionsListUiMapperImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Test Park"
        private const val START_TIME_MS = 1700000000000L
        private const val DISTANCE_KM = 12.345
        private val TEST_LOCALE = Locale.US
    }

    private lateinit var mapper: SessionsListUiMapperImpl

    @Before
    fun setup() {
        mapper = SessionsListUiMapperImpl(
            localizedContextProvider = mockLocalizedContextProvider(TEST_LOCALE),
        )
    }

    @Test
    fun `toUiModel maps id and destination name`() {
        val session = createSession(id = SESSION_ID, destinationName = DESTINATION_NAME)

        val result = mapper.toUiModel(session)

        assertEquals(SESSION_ID, result.id)
        assertEquals(DESTINATION_NAME, result.destinationName)
    }

    @Test
    fun `toUiModel formats date`() {
        val session = createSession(startTimeMs = START_TIME_MS)
        val expectedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", TEST_LOCALE).format(Date(START_TIME_MS))

        val result = mapper.toUiModel(session)

        assertEquals(expectedDate, result.dateFormatted)
    }

    @Test
    fun `toUiModel formats distance with two decimals`() {
        val session = createSession(traveledDistanceKm = DISTANCE_KM)

        val result = mapper.toUiModel(session)

        assertEquals(String.format(Locale.getDefault(), "%.2f", DISTANCE_KM), result.distanceFormatted)
    }

    @Test
    fun `toUiModel maps RUNNING status`() {
        val session = createSession(status = SessionStatus.RUNNING)

        val result = mapper.toUiModel(session)

        assertEquals(SessionListItemStatus.RUNNING, result.status)
    }

    @Test
    fun `toUiModel maps PAUSED status`() {
        val session = createSession(status = SessionStatus.PAUSED)

        val result = mapper.toUiModel(session)

        assertEquals(SessionListItemStatus.PAUSED, result.status)
    }

    @Test
    fun `toUiModel maps COMPLETED status`() {
        val session = createSession(status = SessionStatus.COMPLETED)

        val result = mapper.toUiModel(session)

        assertEquals(SessionListItemStatus.COMPLETED, result.status)
    }

    @Test
    fun `toUiModel shows share button for completed sessions`() {
        val session = createSession(status = SessionStatus.COMPLETED)

        val result = mapper.toUiModel(session)

        assertTrue(result.isShareButtonVisible)
    }

    @Test
    fun `toUiModel hides share button for running sessions`() {
        val session = createSession(status = SessionStatus.RUNNING)

        val result = mapper.toUiModel(session)

        assertFalse(result.isShareButtonVisible)
    }

    @Test
    fun `toUiModel hides share button for paused sessions`() {
        val session = createSession(status = SessionStatus.PAUSED)

        val result = mapper.toUiModel(session)

        assertFalse(result.isShareButtonVisible)
    }
}
