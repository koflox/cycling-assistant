package com.koflox.destinations.data.source.prefs

internal interface PreferencesDataSource {
    suspend fun getLoadedFiles(): Set<String>
    suspend fun addLoadedFile(fileName: String)
}
