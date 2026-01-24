package com.koflox.destinations.data.source.asset

import android.content.Context
import android.content.res.AssetManager
import com.koflox.distance.DistanceCalculator
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DestinationFileResolverImplTest {

    companion object {
        private const val TOKYO_LAT = 35.6812
        private const val TOKYO_LON = 139.7671
        private const val OSAKA_LAT = 34.6937
        private const val OSAKA_LON = 135.5023
        private const val DISTANCE_WITHIN_RADIUS = 50.0
        private const val DISTANCE_OUTSIDE_RADIUS = 150.0
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()
    private val distanceCalculator: DistanceCalculator = mockk()
    private lateinit var resolver: DestinationFileResolverImpl

    @Before
    fun setup() {
        every { context.assets } returns assetManager
        resolver = DestinationFileResolverImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            context = context,
            distanceCalculator = distanceCalculator,
        )
    }

    @Test
    fun `returns empty list when no destination files exist`() = runTest {
        every { assetManager.list("") } returns emptyArray()
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when only non-matching files exist`() = runTest {
        every { assetManager.list("") } returns arrayOf("config.json", "readme.txt")
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parses valid destination file name correctly`() = runTest {
        val fileName = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        every { assetManager.list("") } returns arrayOf(fileName)
        every {
            distanceCalculator.calculateKm(
                lat1 = TOKYO_LAT,
                lon1 = TOKYO_LON,
                lat2 = TOKYO_LAT,
                lon2 = TOKYO_LON,
            )
        } returns 0.0
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(fileName, this.fileName)
            assertEquals("tokyo", city)
            assertEquals("japan", country)
            assertEquals(TOKYO_LAT, centerLatitude, 0.0001)
            assertEquals(TOKYO_LON, centerLongitude, 0.0001)
            assertEquals(1, tier)
        }
    }

    @Test
    fun `returns files sorted by city then tier`() = runTest {
        val tokyoTier1 = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        val tokyoTier2 = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier2.json"
        val osakaTier1 = "destinations_osaka_japan_${OSAKA_LAT}_${OSAKA_LON}_tier1.json"
        every { assetManager.list("") } returns arrayOf(tokyoTier2, osakaTier1, tokyoTier1)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_WITHIN_RADIUS
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(3, result.size)
        assertEquals("osaka", result[0].city)
        assertEquals(1, result[0].tier)
        assertEquals("tokyo", result[1].city)
        assertEquals(1, result[1].tier)
        assertEquals("tokyo", result[2].city)
        assertEquals(2, result[2].tier)
    }

    @Test
    fun `returns only files within 100km radius`() = runTest {
        val tokyoFile = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        val osakaFile = "destinations_osaka_japan_${OSAKA_LAT}_${OSAKA_LON}_tier1.json"
        every { assetManager.list("") } returns arrayOf(tokyoFile, osakaFile)
        every {
            distanceCalculator.calculateKm(
                lat1 = TOKYO_LAT,
                lon1 = TOKYO_LON,
                lat2 = TOKYO_LAT,
                lon2 = TOKYO_LON,
            )
        } returns DISTANCE_WITHIN_RADIUS
        every {
            distanceCalculator.calculateKm(
                lat1 = TOKYO_LAT,
                lon1 = TOKYO_LON,
                lat2 = OSAKA_LAT,
                lon2 = OSAKA_LON,
            )
        } returns DISTANCE_OUTSIDE_RADIUS
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(1, result.size)
        assertEquals("tokyo", result.first().city)
    }

    @Test
    fun `returns empty list when all files are outside radius`() = runTest {
        val tokyoFile = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        every { assetManager.list("") } returns arrayOf(tokyoFile)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns DISTANCE_OUTSIDE_RADIUS
        val userLocation = Location(latitude = 0.0, longitude = 0.0)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `includes files exactly at 100km boundary`() = runTest {
        val tokyoFile = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        every { assetManager.list("") } returns arrayOf(tokyoFile)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 100.0
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(1, result.size)
    }

    @Test
    fun `ignores files with invalid format`() = runTest {
        val validFile = "destinations_tokyo_japan_${TOKYO_LAT}_${TOKYO_LON}_tier1.json"
        val invalidFiles = arrayOf(
            "destinations.json",
            "destinations_tokyo.json",
            "destinations_tokyo_japan_tier1.json",
            "destinations_tokyo_japan_35.6812_tier1.json",
            "other_tokyo_japan_35.6812_139.7671_tier1.json",
        )
        every { assetManager.list("") } returns invalidFiles + validFile
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.0
        val userLocation = Location(latitude = TOKYO_LAT, longitude = TOKYO_LON)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(1, result.size)
        assertEquals(validFile, result.first().fileName)
    }

    @Test
    fun `handles negative coordinates correctly`() = runTest {
        val fileName = "destinations_saopaulo_brazil_-23.5505_-46.6333_tier1.json"
        every { assetManager.list("") } returns arrayOf(fileName)
        every { distanceCalculator.calculateKm(any(), any(), any(), any()) } returns 0.0
        val userLocation = Location(latitude = -23.5505, longitude = -46.6333)

        val result = resolver.getFilesWithinRadius(userLocation)

        assertEquals(1, result.size)
        assertEquals(-23.5505, result.first().centerLatitude, 0.0001)
        assertEquals(-46.6333, result.first().centerLongitude, 0.0001)
    }
}
