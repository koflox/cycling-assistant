package com.koflox.destinations.data.repository

import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.data.source.prefs.PreferencesDataSource
import com.koflox.destinations.testutil.createDestination
import com.koflox.destinations.testutil.createDestinationAsset
import com.koflox.destinations.testutil.createDestinationLocal
import com.koflox.location.LocationDataSource
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DestinationRepositoryImplTest {

    companion object {
        private const val TEST_ID = "test-1"
        private const val TEST_TITLE = "Test Destination"
        private const val TEST_LAT = 52.52
        private const val TEST_LONG = 13.405
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val poiLocalDataSource: PoiLocalDataSource = mockk()
    private val poiAssetDataSource: PoiAssetDataSource = mockk()
    private val locationDataSource: LocationDataSource = mockk()
    private val preferencesDataSource: PreferencesDataSource = mockk()
    private val mapper: DestinationMapper = mockk()
    private lateinit var repository: DestinationRepositoryImpl

    @Before
    fun setup() {
        repository = DestinationRepositoryImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            poiLocalDataSource = poiLocalDataSource,
            poiAssetDataSource = poiAssetDataSource,
            locationDataSource = locationDataSource,
            preferencesDataSource = preferencesDataSource,
            mapper = mapper,
        )
    }

    @Test
    fun `initializeDatabase checks if already initialized`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns true

        repository.initializeDatabase()

        coVerify { preferencesDataSource.isDatabaseInitialized() }
    }

    @Test
    fun `initializeDatabase skips initialization when already done`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns true

        repository.initializeDatabase()

        coVerify(exactly = 0) { poiAssetDataSource.readDestinationsJson() }
        coVerify(exactly = 0) { poiLocalDataSource.insertAll(any()) }
    }

    @Test
    fun `initializeDatabase reads assets when not initialized`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery { poiAssetDataSource.readDestinationsJson() } returns emptyList()
        coEvery { mapper.toLocalList(any()) } returns emptyList()

        repository.initializeDatabase()

        coVerify { poiAssetDataSource.readDestinationsJson() }
    }

    @Test
    fun `initializeDatabase maps assets to entities`() = runTest {
        val assets = listOf(createDestinationAsset(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery { poiAssetDataSource.readDestinationsJson() } returns assets
        coEvery {
            mapper.toLocalList(assets)
        } returns listOf(createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))

        repository.initializeDatabase()

        coVerify { mapper.toLocalList(assets) }
    }

    @Test
    fun `initializeDatabase inserts entities into database`() = runTest {
        val entities = listOf(createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery {
            poiAssetDataSource.readDestinationsJson()
        } returns listOf(createDestinationAsset(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        coEvery { mapper.toLocalList(any()) } returns entities

        repository.initializeDatabase()

        coVerify { poiLocalDataSource.insertAll(entities) }
    }

    @Test
    fun `initializeDatabase sets initialized flag after success`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery { poiAssetDataSource.readDestinationsJson() } returns emptyList()
        coEvery { mapper.toLocalList(any()) } returns emptyList()
        coJustRun { poiLocalDataSource.insertAll(any()) }

        repository.initializeDatabase()

        coVerify { preferencesDataSource.setDatabaseInitialized(true) }
    }

    @Test
    fun `initializeDatabase returns success when already initialized`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns true

        val result = repository.initializeDatabase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `initializeDatabase returns success after initialization`() = runTest {
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery { poiAssetDataSource.readDestinationsJson() } returns emptyList()
        coEvery { mapper.toLocalList(any()) } returns emptyList()
        coJustRun { poiLocalDataSource.insertAll(any()) }
        coJustRun { preferencesDataSource.setDatabaseInitialized(any()) }

        val result = repository.initializeDatabase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `initializeDatabase returns failure on error`() = runTest {
        val exception = RuntimeException("Asset read failed")
        coEvery { preferencesDataSource.isDatabaseInitialized() } returns false
        coEvery { poiAssetDataSource.readDestinationsJson() } throws exception

        val result = repository.initializeDatabase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getAllDestinations fetches from poiLocalDataSource`() = runTest {
        coEvery { poiLocalDataSource.getAllDestinations() } returns emptyList()

        repository.getAllDestinations()

        coVerify { poiLocalDataSource.getAllDestinations() }
    }

    @Test
    fun `getAllDestinations maps entities to domain`() = runTest {
        val entity = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        val domain = createDestination(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        coEvery { poiLocalDataSource.getAllDestinations() } returns listOf(entity)
        coEvery { mapper.toDomain(entity) } returns domain

        repository.getAllDestinations()

        coVerify { mapper.toDomain(entity) }
    }

    @Test
    fun `getAllDestinations returns mapped destinations`() = runTest {
        val entity = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        val domain = createDestination(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        coEvery { poiLocalDataSource.getAllDestinations() } returns listOf(entity)
        coEvery { mapper.toDomain(entity) } returns domain

        val result = repository.getAllDestinations()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(domain, result.getOrNull()?.get(0))
    }

    @Test
    fun `getAllDestinations returns empty list when no destinations`() = runTest {
        coEvery { poiLocalDataSource.getAllDestinations() } returns emptyList()

        val result = repository.getAllDestinations()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `getAllDestinations returns failure on error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { poiLocalDataSource.getAllDestinations() } throws exception

        val result = repository.getAllDestinations()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getUserLocation delegates to locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { locationDataSource.getCurrentLocation() } returns Result.success(location)

        repository.getUserLocation()

        coVerify { locationDataSource.getCurrentLocation() }
    }

    @Test
    fun `getUserLocation returns location on success`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { locationDataSource.getCurrentLocation() } returns Result.success(location)

        val result = repository.getUserLocation()

        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }

    @Test
    fun `getUserLocation returns failure on error`() = runTest {
        val exception = RuntimeException("Location unavailable")
        coEvery { locationDataSource.getCurrentLocation() } returns Result.failure(exception)

        val result = repository.getUserLocation()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeUserLocation delegates to locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        every { locationDataSource.observeLocationUpdates(any(), any()) } returns flowOf(location)

        repository.observeUserLocation()

        verify { locationDataSource.observeLocationUpdates() }
    }

    @Test
    fun `observeUserLocation returns flow from locationDataSource`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val flow = flowOf(location)
        every { locationDataSource.observeLocationUpdates(any(), any()) } returns flow

        val result = repository.observeUserLocation()

        assertEquals(flow, result)
    }
}
