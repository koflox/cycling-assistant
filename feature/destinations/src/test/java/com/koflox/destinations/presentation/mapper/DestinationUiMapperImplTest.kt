package com.koflox.destinations.presentation.mapper

import android.content.Context
import com.koflox.destinations.R
import com.koflox.destinations.domain.model.Destinations
import com.koflox.destinations.testutil.createDestination
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DestinationUiMapperImplTest {

    companion object {
        private const val MAIN_ID = "main-dest"
        private const val MAIN_TITLE = "Main Destination"
        private const val MAIN_LAT = 52.52
        private const val MAIN_LON = 13.405
        private const val OTHER_ID = "other-dest"
        private const val OTHER_TITLE = "Other Destination"
        private const val OTHER_LAT = 48.856
        private const val OTHER_LON = 2.352
        private const val USER_LAT = 50.0
        private const val USER_LON = 10.0
        private const val MAIN_DISTANCE = 5.0
        private const val OTHER_DISTANCE = 12.0
        private const val DISTANCE_FORMAT = "%.1f km away"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val distanceCalculator: DistanceCalculator = mockk()
    private val context: Context = mockk()
    private lateinit var mapper: DestinationUiMapperImpl

    @Before
    fun setup() {
        every { context.getString(R.string.distance_to_dest_desc) } returns DISTANCE_FORMAT
        mapper = DestinationUiMapperImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            distanceCalculator = distanceCalculator,
            context = context,
        )
    }

    @Test
    fun `toUiModel maps main destination with isMain true`() = runTest {
        val destinations = createDestinations()
        setupDistanceMocks()

        val result = mapper.toUiModel(destinations, createUserLocation())

        assertTrue(result.selected.isMain)
        assertEquals(MAIN_ID, result.selected.id)
        assertEquals(MAIN_TITLE, result.selected.title)
        assertEquals(MAIN_DISTANCE, result.selected.distanceKm, 0.0)
        assertEquals("5.0 km away", result.selected.distanceFormatted)
    }

    @Test
    fun `toUiModel maps main destination location`() = runTest {
        val destinations = createDestinations()
        setupDistanceMocks()

        val result = mapper.toUiModel(destinations, createUserLocation())

        assertEquals(MAIN_LAT, result.selected.location.latitude, 0.0)
        assertEquals(MAIN_LON, result.selected.location.longitude, 0.0)
    }

    @Test
    fun `toUiModel maps other destinations with isMain false`() = runTest {
        val destinations = createDestinations()
        setupDistanceMocks()

        val result = mapper.toUiModel(destinations, createUserLocation())

        assertEquals(1, result.otherValidDestinations.size)
        assertFalse(result.otherValidDestinations[0].isMain)
        assertEquals(OTHER_ID, result.otherValidDestinations[0].id)
        assertEquals(OTHER_DISTANCE, result.otherValidDestinations[0].distanceKm, 0.0)
    }

    @Test
    fun `toUiModel handles empty other destinations`() = runTest {
        val destinations = Destinations(
            mainDestination = createDestination(id = MAIN_ID, title = MAIN_TITLE, latitude = MAIN_LAT, longitude = MAIN_LON),
            otherValidDestinations = emptyList(),
        )
        every {
            distanceCalculator.calculateKm(USER_LAT, USER_LON, MAIN_LAT, MAIN_LON)
        } returns MAIN_DISTANCE

        val result = mapper.toUiModel(destinations, createUserLocation())

        assertTrue(result.otherValidDestinations.isEmpty())
    }

    private fun createDestinations() = Destinations(
        mainDestination = createDestination(id = MAIN_ID, title = MAIN_TITLE, latitude = MAIN_LAT, longitude = MAIN_LON),
        otherValidDestinations = listOf(
            createDestination(id = OTHER_ID, title = OTHER_TITLE, latitude = OTHER_LAT, longitude = OTHER_LON),
        ),
    )

    private fun createUserLocation() = Location(latitude = USER_LAT, longitude = USER_LON)

    private fun setupDistanceMocks() {
        every { distanceCalculator.calculateKm(USER_LAT, USER_LON, MAIN_LAT, MAIN_LON) } returns MAIN_DISTANCE
        every { distanceCalculator.calculateKm(USER_LAT, USER_LON, OTHER_LAT, OTHER_LON) } returns OTHER_DISTANCE
    }
}
