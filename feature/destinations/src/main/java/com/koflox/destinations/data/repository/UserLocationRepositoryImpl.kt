package com.koflox.destinations.data.repository

import com.koflox.destinations.domain.repository.UserLocationRepository
import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class UserLocationRepositoryImpl(
    private val dispatcherDefault: CoroutineDispatcher,
    private val locationDataSource: LocationDataSource,
) : UserLocationRepository {

    override suspend fun getUserLocation(): Result<Location> = withContext(dispatcherDefault) {
        locationDataSource.getCurrentLocation()
    }

    override fun observeUserLocation(): Flow<Location> = locationDataSource.observeLocationUpdates()
}
