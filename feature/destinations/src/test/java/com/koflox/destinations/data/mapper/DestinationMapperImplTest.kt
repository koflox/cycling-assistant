package com.koflox.destinations.data.mapper

import com.koflox.destinations.data.source.asset.model.DestinationAsset
import com.koflox.destinations.data.source.local.entity.DestinationLocal
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
        val entity = createLocal()

        val result = mapper.toDomain(entity)

        assertEquals(TEST_ID, result.id)
        assertEquals(TEST_TITLE, result.title)
        assertEquals(TEST_LAT, result.latitude, 0.0)
        assertEquals(TEST_LONG, result.longitude, 0.0)
    }

    @Test
    fun `toEntity maps asset correctly`() = runTest {
        val asset = createAsset()

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
        val assets = listOf(createAsset())

        val result = mapper.toLocalList(assets)

        assertEquals(1, result.size)
        assertEquals(TEST_ID, result[0].id)
    }

    @Test
    fun `toEntityList maps multiple items`() = runTest {
        val assets = listOf(
            createAsset(id = "id-1"),
            createAsset(id = "id-2"),
            createAsset(id = "id-3"),
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
            createAsset(id = "z-last"),
            createAsset(id = "a-first"),
            createAsset(id = "m-middle"),
        )

        val result = mapper.toLocalList(assets)

        assertEquals("z-last", result[0].id)
        assertEquals("a-first", result[1].id)
        assertEquals("m-middle", result[2].id)
    }

    private fun createLocal(
        id: String = TEST_ID,
        title: String = TEST_TITLE,
        latitude: Double = TEST_LAT,
        longitude: Double = TEST_LONG,
    ) = DestinationLocal(
        id = id,
        title = title,
        latitude = latitude,
        longitude = longitude,
    )

    private fun createAsset(
        id: String = TEST_ID,
        title: String = TEST_TITLE,
        latitude: Double = TEST_LAT,
        longitude: Double = TEST_LONG,
    ) = DestinationAsset(
        id = id,
        title = title,
        latitude = latitude,
        longitude = longitude,
    )
}
