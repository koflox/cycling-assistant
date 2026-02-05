package com.koflox.location.settings

import kotlinx.coroutines.flow.Flow

interface LocationSettingsDataSource {
    fun isLocationEnabled(): Boolean
    fun observeLocationEnabled(): Flow<Boolean>
    suspend fun resolveLocationSettings(): LocationSettingsResult
}
