package com.koflox.destinations.data.repository

import app.cash.turbine.test
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.source.asset.DestinationFileResolver
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.asset.model.DestinationFileMetadata
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.data.source.prefs.PreferencesDataSource
import com.koflox.destinations.domain.model.DestinationLoadingEvent
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
import kotlinx.coroutines.sync.Mutex
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
        private const val TEST_FILE_NAME = "destinations_tokyo_japan_35.6812_139.7671_tier1.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val poiLocalDataSource: PoiLocalDataSource = mockk()
    private val poiAssetDataSource: PoiAssetDataSource = mockk()
    private val locationDataSource: LocationDataSource = mockk()
    private val preferencesDataSource: PreferencesDataSource = mockk()
    private val destinationFileResolver: DestinationFileResolver = mockk()
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
            destinationFileResolver = destinationFileResolver,
            mapper = mapper,
            mutex = Mutex(),
        )
    }

    private fun createFileMetadata(
        fileName: String = TEST_FILE_NAME,
        tier: Int = 1,
    ) = DestinationFileMetadata(
        fileName = fileName,
        city = "tokyo",
        country = "japan",
        centerLatitude = 35.6812,
        centerLongitude = 139.7671,
        tier = tier,
    )

    @Test
    fun `loadDestinationsForLocation gets loaded files from preferences`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns emptyList()

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify { preferencesDataSource.getLoadedFiles() }
    }

    @Test
    fun `loadDestinationsForLocation resolves files for nearest city`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns emptyList()

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify { destinationFileResolver.getFilesWithinRadius(location) }
    }

    @Test
    fun `loadDestinationsForLocation skips already loaded files`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val fileMetadata = createFileMetadata()
        coEvery { preferencesDataSource.getLoadedFiles() } returns setOf(TEST_FILE_NAME)
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(fileMetadata)

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify(exactly = 0) { poiAssetDataSource.readDestinationsJson(any()) }
    }

    @Test
    fun `loadDestinationsForLocation reads and inserts new files`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val fileMetadata = createFileMetadata()
        val assets = listOf(createDestinationAsset(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        val entities = listOf(createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG))
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(fileMetadata)
        coEvery { poiAssetDataSource.readDestinationsJson(TEST_FILE_NAME) } returns assets
        coEvery { mapper.toLocalList(assets) } returns entities
        coJustRun { poiLocalDataSource.insertAll(entities) }
        coJustRun { preferencesDataSource.addLoadedFile(TEST_FILE_NAME) }

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify { poiAssetDataSource.readDestinationsJson(TEST_FILE_NAME) }
        coVerify { poiLocalDataSource.insertAll(entities) }
        coVerify { preferencesDataSource.addLoadedFile(TEST_FILE_NAME) }
    }

    @Test
    fun `loadDestinationsForLocation loads files in order`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val tier1File = createFileMetadata(fileName = "tier1.json", tier = 1)
        val tier2File = createFileMetadata(fileName = "tier2.json", tier = 2)
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(tier1File, tier2File)
        coEvery { poiAssetDataSource.readDestinationsJson(any()) } returns emptyList()
        coEvery { mapper.toLocalList(any()) } returns emptyList()
        coJustRun { poiLocalDataSource.insertAll(any()) }
        coJustRun { preferencesDataSource.addLoadedFile(any()) }

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            poiAssetDataSource.readDestinationsJson("tier1.json")
            poiAssetDataSource.readDestinationsJson("tier2.json")
        }
    }

    @Test
    fun `loadDestinationsForLocation emits Completed when no files to load`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns emptyList()

        repository.loadDestinationsForLocation(location).test {
            assertEquals(DestinationLoadingEvent.Loading, awaitItem())
            assertEquals(DestinationLoadingEvent.Completed, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `loadDestinationsForLocation emits Error on failure`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val exception = RuntimeException("Asset read failed")
        val fileMetadata = createFileMetadata()
        coEvery { preferencesDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(fileMetadata)
        coEvery { poiAssetDataSource.readDestinationsJson(any()) } throws exception

        repository.loadDestinationsForLocation(location).test {
            assertEquals(DestinationLoadingEvent.Loading, awaitItem())
            val errorEvent = awaitItem() as DestinationLoadingEvent.Error
            assertEquals(exception, errorEvent.throwable)
            awaitComplete()
        }
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
