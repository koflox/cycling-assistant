package com.koflox.destinations.data.repository

import com.koflox.concurrent.suspendRunCatching
import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.source.asset.DestinationFileResolver
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.local.DestinationFilesLocalDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.model.DestinationLoadingEvent
import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class DestinationRepositoryImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val poiLocalDataSource: PoiLocalDataSource,
    private val poiAssetDataSource: PoiAssetDataSource,
    private val locationDataSource: LocationDataSource,
    private val destinationFilesLocalDataSource: DestinationFilesLocalDataSource,
    private val destinationFileResolver: DestinationFileResolver,
    private val mapper: DestinationMapper,
    private val mutex: Mutex,
) : DestinationRepository {

    override fun loadDestinationsForLocation(location: Location): Flow<DestinationLoadingEvent> =
        flow {
            mutex.withLock {
                emit(DestinationLoadingEvent.Loading)
                try {
                    val loadedFiles = destinationFilesLocalDataSource.getLoadedFiles()
                    val files = destinationFileResolver.getFilesWithinRadius(location)
                    val filesToLoad = files.filter { it.fileName !in loadedFiles }
                    for (file in filesToLoad) {
                        val jsonData = poiAssetDataSource.readDestinationsJson(file.fileName)
                        val entities = mapper.toLocalList(jsonData)
                        poiLocalDataSource.insertAll(entities)
                        destinationFilesLocalDataSource.addLoadedFile(file.fileName)
                    }
                    emit(DestinationLoadingEvent.Completed)
                } catch (e: Exception) {
                    emit(DestinationLoadingEvent.Error(e))
                }
            }
        }.flowOn(dispatcherDefault)

    override suspend fun getDestinationsInArea(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
    ): Result<List<Destination>> = withContext(dispatcherDefault) {
        suspendRunCatching {
            poiLocalDataSource.getDestinationsInArea(minLat, maxLat, minLon, maxLon).map {
                mapper.toDomain(it)
            }
        }
    }

    override suspend fun getDestinationById(id: String): Result<Destination?> = withContext(dispatcherDefault) {
        suspendRunCatching {
            poiLocalDataSource.getDestinationById(id)?.let { mapper.toDomain(it) }
        }
    }

    override suspend fun getUserLocation(): Result<Location> = withContext(dispatcherDefault) {
        locationDataSource.getCurrentLocation()
    }

    override fun observeUserLocation(): Flow<Location> = locationDataSource.observeLocationUpdates()
}
