package com.koflox.session.domain.usecase

import com.koflox.location.settings.LocationSettingsDataSource
import kotlinx.coroutines.flow.Flow

interface CheckLocationEnabledUseCase {
    fun isLocationEnabled(): Boolean
    fun observeLocationEnabled(): Flow<Boolean>
}

internal class CheckLocationEnabledUseCaseImpl(
    private val locationSettingsDataSource: LocationSettingsDataSource,
) : CheckLocationEnabledUseCase {

    override fun isLocationEnabled(): Boolean =
        locationSettingsDataSource.isLocationEnabled()

    override fun observeLocationEnabled(): Flow<Boolean> =
        locationSettingsDataSource.observeLocationEnabled()
}
