package com.koflox.destinations.data.source.asset

import android.content.Context
import android.content.res.AssetManager
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream

class PoiAssetDataSourceImplTest {

    companion object {
        private const val TEST_FILE_NAME = "destinations_tokyo_japan_35.6812_139.7671_tier1.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()
    private lateinit var dataSource: PoiAssetDataSourceImpl

    @Before
    fun setup() {
        every { context.assets } returns assetManager
        dataSource = PoiAssetDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            context = context,
        )
    }

    @Test
    fun `readDestinationsJson opens specified file`() = runTest {
        val json = "[]"
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        dataSource.readDestinationsJson(TEST_FILE_NAME)

        verify { assetManager.open(TEST_FILE_NAME) }
    }

    @Test
    fun `readDestinationsJson returns empty list for empty array`() = runTest {
        val json = "[]"
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = dataSource.readDestinationsJson(TEST_FILE_NAME)

        assertEquals(0, result.size)
    }

    @Test
    fun `readDestinationsJson parses single destination`() = runTest {
        val json = """
            [
                {
                    "id": "test-1",
                    "title": "Test Destination",
                    "lat": 52.52,
                    "long": 13.405
                }
            ]
        """.trimIndent()
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = dataSource.readDestinationsJson(TEST_FILE_NAME)

        assertEquals(1, result.size)
        assertEquals("test-1", result[0].id)
        assertEquals("Test Destination", result[0].title)
        assertEquals(52.52, result[0].latitude, 0.0)
        assertEquals(13.405, result[0].longitude, 0.0)
    }

    @Test
    fun `readDestinationsJson parses multiple destinations`() = runTest {
        val json = """
            [
                {"id": "1", "title": "First", "lat": 10.0, "long": 20.0},
                {"id": "2", "title": "Second", "lat": 30.0, "long": 40.0},
                {"id": "3", "title": "Third", "lat": 50.0, "long": 60.0}
            ]
        """.trimIndent()
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = dataSource.readDestinationsJson(TEST_FILE_NAME)

        assertEquals(3, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
        assertEquals("3", result[2].id)
    }

    @Test
    fun `readDestinationsJson handles negative coordinates`() = runTest {
        val json = """
            [
                {"id": "1", "title": "Sydney", "lat": -33.8688, "long": 151.2093}
            ]
        """.trimIndent()
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = dataSource.readDestinationsJson(TEST_FILE_NAME)

        assertEquals(-33.8688, result[0].latitude, 0.0001)
        assertEquals(151.2093, result[0].longitude, 0.0001)
    }

    @Test
    fun `readDestinationsJson preserves order`() = runTest {
        val json = """
            [
                {"id": "z", "title": "Z", "lat": 0.0, "long": 0.0},
                {"id": "a", "title": "A", "lat": 0.0, "long": 0.0},
                {"id": "m", "title": "M", "lat": 0.0, "long": 0.0}
            ]
        """.trimIndent()
        every { assetManager.open(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = dataSource.readDestinationsJson(TEST_FILE_NAME)

        assertEquals("z", result[0].id)
        assertEquals("a", result[1].id)
        assertEquals("m", result[2].id)
    }
}
