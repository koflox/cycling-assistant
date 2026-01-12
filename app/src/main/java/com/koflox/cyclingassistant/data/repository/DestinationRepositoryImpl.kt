package com.koflox.cyclingassistant.data.repository

import com.koflox.cyclingassistant.data.mapper.DestinationMapper
import com.koflox.cyclingassistant.data.source.prefs.PreferencesDataSource
import com.koflox.cyclingassistant.data.source.asset.PoiAssetDataSource
import com.koflox.cyclingassistant.data.source.local.database.dao.DestinationDao
import com.koflox.cyclingassistant.data.source.location.LocationDataSource
import com.koflox.cyclingassistant.domain.model.Destination
import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.domain.repository.DestinationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DestinationRepositoryImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val dao: DestinationDao,
    private val poiAssetDataSource: PoiAssetDataSource,
    private val locationDataSource: LocationDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val mapper: DestinationMapper,
) : DestinationRepository {

    override suspend fun initializeDatabase(): Result<Unit> = withContext(dispatcherDefault) {
        runCatching {
            if (!preferencesDataSource.isDatabaseInitialized()) {
                val jsonData = poiAssetDataSource.readDestinationsJson()
                val entities = mapper.toEntityList(jsonData)
                dao.insertAll(entities)
                preferencesDataSource.setDatabaseInitialized(true)
            }
        }
    }

    override suspend fun getAllDestinations(): Result<List<Destination>> = withContext(dispatcherDefault) {
        runCatching {
            dao.getAllDestinations().map {
                mapper.toDomain(it)
            }
        }
    }

    override suspend fun getUserLocation(): Result<Location> = withContext(dispatcherDefault) {
        locationDataSource.getCurrentLocation()
    }

}
