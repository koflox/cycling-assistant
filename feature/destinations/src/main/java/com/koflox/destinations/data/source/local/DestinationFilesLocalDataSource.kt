package com.koflox.destinations.data.source.local

internal interface DestinationFilesLocalDataSource {
    suspend fun getLoadedFiles(): Set<String>
    suspend fun addLoadedFile(fileName: String)
}
