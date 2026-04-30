package com.koflox.location.settings

import kotlinx.coroutines.flow.Flow

internal interface LocationSettingsDataSource {
    fun isLocationEnabled(): Boolean
    fun observeLocationEnabled(): Flow<Boolean>
    suspend fun resolveLocationSettings(): LocationSettingsResult
}
