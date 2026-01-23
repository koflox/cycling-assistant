package com.koflox.destinations.data.mapper

import com.koflox.destinations.data.source.asset.model.DestinationAsset
import com.koflox.destinations.testutil.createDestinationAsset
import com.koflox.destinations.testutil.createDestinationLocal
import com.koflox.testing.coroutine.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DestinationMapperImplTest {

    companion object {
        private const val TEST_ID = "test-id-1"
        private const val TEST_TITLE = "Test Destination"
        private const val TEST_LAT = 52.52
        private const val TEST_LONG = 13.405
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mapper: DestinationMapperImpl

    @Before
    fun setup() {
        mapper = DestinationMapperImpl(mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `toDomain maps entity correctly`() = runTest {
        val entity = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)

        val result = mapper.toDomain(entity)

        assertEquals(TEST_ID, result.id)
        assertEquals(TEST_TITLE, result.title)
        assertEquals(TEST_LAT, result.latitude, 0.0)
        assertEquals(TEST_LONG, result.longitude, 0.0)
    }

    @Test
    fun `toEntity maps asset correctly`() = runTest {
        val asset = createDestinationAsset(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)

        val result = mapper.toLocal(asset)

        assertEquals(TEST_ID, result.id)
        assertEquals(TEST_TITLE, result.title)
        assertEquals(TEST_LAT, result.latitude, 0.0)
        assertEquals(TEST_LONG, result.longitude, 0.0)
    }

    @Test
    fun `toEntityList maps empty list`() = runTest {
        val assets = emptyList<DestinationAsset>()

        val result = mapper.toLocalList(assets)

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntityList maps single item`() = runTest {
        val assets = listOf(createDestinationAsset(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))

        val result = mapper.toLocalList(assets)

        assertEquals(1, result.size)
        assertEquals(TEST_ID, result[0].id)
    }

    @Test
    fun `toEntityList maps multiple items`() = runTest {
        val assets = listOf(
            createDestinationAsset(id = "id-1", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationAsset(id = "id-2", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationAsset(id = "id-3", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
        )

        val result = mapper.toLocalList(assets)

        assertEquals(3, result.size)
        assertEquals("id-1", result[0].id)
        assertEquals("id-2", result[1].id)
        assertEquals("id-3", result[2].id)
    }

    @Test
    fun `toEntityList preserves order`() = runTest {
        val assets = listOf(
            createDestinationAsset(id = "z-last", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationAsset(id = "a-first", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationAsset(id = "m-middle", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
        )

        val result = mapper.toLocalList(assets)

        assertEquals("z-last", result[0].id)
        assertEquals("a-first", result[1].id)
        assertEquals("m-middle", result[2].id)
    }
}
