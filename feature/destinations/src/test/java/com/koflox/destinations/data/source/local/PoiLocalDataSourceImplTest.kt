package com.koflox.destinations.data.source.local

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.destinations.testutil.createDestinationLocal
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PoiLocalDataSourceImplTest {

    companion object {
        private const val TEST_ID = "test-1"
        private const val TEST_TITLE = "Test Destination"
        private const val TEST_LAT = 52.52
        private const val TEST_LONG = 13.405
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao: DestinationDao = mockk()
    private val daoFactory = object : ConcurrentFactory<DestinationDao>() {
        override suspend fun create(): DestinationDao = dao
    }
    private lateinit var dataSource: PoiLocalDataSourceImpl

    @Before
    fun setup() {
        dataSource = PoiLocalDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            daoFactory = daoFactory,
        )
    }

    @Test
    fun `getDestinationsInArea delegates to dao`() = runTest {
        coEvery { dao.getDestinationsInArea(any(), any(), any(), any()) } returns emptyList()

        dataSource.getDestinationsInArea(
            minLat = 50.0,
            maxLat = 55.0,
            minLon = 10.0,
            maxLon = 15.0,
        )

        coVerify {
            dao.getDestinationsInArea(
                minLat = 50.0,
                maxLat = 55.0,
                minLon = 10.0,
                maxLon = 15.0,
            )
        }
    }

    @Test
    fun `getDestinationsInArea returns destinations from dao`() = runTest {
        val destinations = listOf(
            createDestinationLocal(id = "1", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
        )
        coEvery { dao.getDestinationsInArea(any(), any(), any(), any()) } returns destinations

        val result = dataSource.getDestinationsInArea(
            minLat = 50.0,
            maxLat = 55.0,
            minLon = 10.0,
            maxLon = 15.0,
        )

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun `getDestinationById delegates to dao`() = runTest {
        val destination = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        coEvery { dao.getDestinationById(TEST_ID) } returns destination

        val result = dataSource.getDestinationById(TEST_ID)

        assertEquals(destination, result)
        coVerify { dao.getDestinationById(TEST_ID) }
    }

    @Test
    fun `getDestinationById returns null when not found`() = runTest {
        coEvery { dao.getDestinationById(any()) } returns null

        val result = dataSource.getDestinationById("missing")

        assertEquals(null, result)
    }

    @Test
    fun `insertAll delegates to dao`() = runTest {
        val destinations = listOf(createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        coJustRun { dao.insertAll(any()) }

        dataSource.insertAll(destinations)

        coVerify { dao.insertAll(destinations) }
    }

    @Test
    fun `insertAll passes correct destinations to dao`() = runTest {
        val destinations = listOf(
            createDestinationLocal(id = "a", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationLocal(id = "b", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationLocal(id = "c", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
        )
        coJustRun { dao.insertAll(any()) }

        dataSource.insertAll(destinations)

        coVerify { dao.insertAll(destinations) }
    }

    @Test
    fun `insertAll handles empty list`() = runTest {
        val destinations = emptyList<DestinationLocal>()
        coJustRun { dao.insertAll(any()) }

        dataSource.insertAll(destinations)

        coVerify { dao.insertAll(destinations) }
    }
}
