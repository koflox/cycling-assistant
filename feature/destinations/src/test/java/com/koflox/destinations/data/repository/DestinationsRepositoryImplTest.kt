package com.koflox.destinations.data.repository

import app.cash.turbine.test
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.source.asset.DestinationFileResolver
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.asset.model.DestinationFileMetadata
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.testutil.createDestination
import com.koflox.destinations.testutil.createDestinationAsset
import com.koflox.destinations.testutil.createDestinationLocal
import com.koflox.location.model.Location
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DestinationsRepositoryImplTest {

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
    private val destinationFilesLocalDataSource: DestinationFilesLocalDataSource = mockk()
    private val destinationFileResolver: DestinationFileResolver = mockk()
    private val mapper: DestinationMapper = mockk()
    private lateinit var repository: DestinationsRepositoryImpl

    @Before
    fun setup() {
        repository = DestinationsRepositoryImpl(
            dispatcherDefault = mainDispatcherRule.testDispatcher,
            poiLocalDataSource = poiLocalDataSource,
            poiAssetDataSource = poiAssetDataSource,
            destinationFilesLocalDataSource = destinationFilesLocalDataSource,
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
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns emptyList()

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify { destinationFilesLocalDataSource.getLoadedFiles() }
    }

    @Test
    fun `loadDestinationsForLocation resolves files for nearest city`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
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
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns setOf(TEST_FILE_NAME)
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
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(fileMetadata)
        coEvery { poiAssetDataSource.readDestinationsJson(TEST_FILE_NAME) } returns assets
        coEvery { mapper.toLocalList(assets) } returns entities
        coJustRun { poiLocalDataSource.insertAll(entities) }
        coJustRun { destinationFilesLocalDataSource.addLoadedFile(TEST_FILE_NAME) }

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify { poiAssetDataSource.readDestinationsJson(TEST_FILE_NAME) }
        coVerify { poiLocalDataSource.insertAll(entities) }
        coVerify { destinationFilesLocalDataSource.addLoadedFile(TEST_FILE_NAME) }
    }

    @Test
    fun `loadDestinationsForLocation loads files in order`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        val tier1File = createFileMetadata(fileName = "tier1.json", tier = 1)
        val tier2File = createFileMetadata(fileName = "tier2.json", tier = 2)
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
        coEvery { destinationFileResolver.getFilesWithinRadius(location) } returns listOf(tier1File, tier2File)
        coEvery { poiAssetDataSource.readDestinationsJson(any()) } returns emptyList()
        coEvery { mapper.toLocalList(any()) } returns emptyList()
        coJustRun { poiLocalDataSource.insertAll(any()) }
        coJustRun { destinationFilesLocalDataSource.addLoadedFile(any()) }

        repository.loadDestinationsForLocation(location).test {
            awaitItem() // Loading
            awaitItem() // Completed
            awaitComplete()
        }

        coVerify(ordering = Ordering.ORDERED) {
            poiAssetDataSource.readDestinationsJson("tier1.json")
            poiAssetDataSource.readDestinationsJson("tier2.json")
        }
    }

    @Test
    fun `loadDestinationsForLocation emits Completed when no files to load`() = runTest {
        val location = Location(TEST_LAT, TEST_LONG)
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
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
        coEvery { destinationFilesLocalDataSource.getLoadedFiles() } returns emptySet()
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
    fun `getDestinationsInArea delegates to poiLocalDataSource`() = runTest {
        val entity = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        val domain = createDestination(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        coEvery { poiLocalDataSource.getDestinationsInArea(any(), any(), any(), any()) } returns listOf(entity)
        coEvery { mapper.toDomain(entity) } returns domain

        val result = repository.getDestinationsInArea(minLat = 50.0, maxLat = 55.0, minLon = 10.0, maxLon = 15.0)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(domain, result.getOrNull()?.get(0))
        coVerify { poiLocalDataSource.getDestinationsInArea(50.0, 55.0, 10.0, 15.0) }
    }

    @Test
    fun `getDestinationsInArea returns empty list when no destinations`() = runTest {
        coEvery { poiLocalDataSource.getDestinationsInArea(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getDestinationsInArea(minLat = 50.0, maxLat = 55.0, minLon = 10.0, maxLon = 15.0)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `getDestinationsInArea returns failure on error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { poiLocalDataSource.getDestinationsInArea(any(), any(), any(), any()) } throws exception

        val result = repository.getDestinationsInArea(minLat = 50.0, maxLat = 55.0, minLon = 10.0, maxLon = 15.0)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getDestinationById delegates to poiLocalDataSource and maps to domain`() = runTest {
        val entity = createDestinationLocal(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        val domain = createDestination(id = TEST_ID, title = TEST_TITLE, latitude = TEST_LAT, longitude = TEST_LONG)
        coEvery { poiLocalDataSource.getDestinationById(TEST_ID) } returns entity
        coEvery { mapper.toDomain(entity) } returns domain

        val result = repository.getDestinationById(TEST_ID)

        assertTrue(result.isSuccess)
        assertEquals(domain, result.getOrNull())
        coVerify { poiLocalDataSource.getDestinationById(TEST_ID) }
    }

    @Test
    fun `getDestinationById returns null when not found`() = runTest {
        coEvery { poiLocalDataSource.getDestinationById(any()) } returns null

        val result = repository.getDestinationById("missing")

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `getDestinationById returns failure on error`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { poiLocalDataSource.getDestinationById(any()) } throws exception

        val result = repository.getDestinationById(TEST_ID)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
