package com.koflox.destinations.data.source.local

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
    private lateinit var dataSource: PoiLocalDataSourceImpl

    @Before
    fun setup() {
        dataSource = PoiLocalDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            dao = dao,
        )
    }

    @Test
    fun `getAllDestinations delegates to dao`() = runTest {
        coEvery { dao.getAllDestinations() } returns emptyList()

        dataSource.getAllDestinations()

        coVerify { dao.getAllDestinations() }
    }

    @Test
    fun `getAllDestinations returns empty list when dao returns empty`() = runTest {
        coEvery { dao.getAllDestinations() } returns emptyList()

        val result = dataSource.getAllDestinations()

        assertEquals(0, result.size)
    }

    @Test
    fun `getAllDestinations returns destinations from dao`() = runTest {
        val destinations = listOf(
            createDestinationLocal(id = "1", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
            createDestinationLocal(id = "2", title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG),
        )
        coEvery { dao.getAllDestinations() } returns destinations

        val result = dataSource.getAllDestinations()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
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
