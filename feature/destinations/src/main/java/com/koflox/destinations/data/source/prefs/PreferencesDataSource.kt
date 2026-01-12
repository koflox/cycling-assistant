package com.koflox.destinations.data.source.prefs

internal interface PreferencesDataSource {
    suspend fun isDatabaseInitialized(): Boolean
    suspend fun setDatabaseInitialized(initialized: Boolean)
}
