package com.koflox.destinations.data.repository

import com.koflox.destinations.data.mapper.DestinationMapper
import com.koflox.destinations.data.source.asset.PoiAssetDataSource
import com.koflox.destinations.data.source.local.PoiLocalDataSource
import com.koflox.destinations.data.source.prefs.PreferencesDataSource
import com.koflox.destinations.domain.model.Destination
import com.koflox.destinations.domain.repository.DestinationRepository
import com.koflox.location.LocationDataSource
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class DestinationRepositoryImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val poiLocalDataSource: PoiLocalDataSource,
    private val poiAssetDataSource: PoiAssetDataSource,
    private val locationDataSource: LocationDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val mapper: DestinationMapper,
) : DestinationRepository {

    override suspend fun initializeDatabase(): Result<Unit> = withContext(dispatcherDefault) {
        runCatching {
            if (!preferencesDataSource.isDatabaseInitialized()) {
                val jsonData = poiAssetDataSource.readDestinationsJson()
                val entities = mapper.toLocalList(jsonData)
                poiLocalDataSource.insertAll(entities)
                preferencesDataSource.setDatabaseInitialized(true)
            }
        }
    }

    override suspend fun getAllDestinations(): Result<List<Destination>> = withContext(dispatcherDefault) {
        runCatching {
            poiLocalDataSource.getAllDestinations().map {
                mapper.toDomain(it)
            }
        }
    }

    override suspend fun getUserLocation(): Result<Location> = withContext(dispatcherDefault) {
        locationDataSource.getCurrentLocation()
    }

    override fun observeUserLocation(): Flow<Location> = locationDataSource.observeLocationUpdates()
}
