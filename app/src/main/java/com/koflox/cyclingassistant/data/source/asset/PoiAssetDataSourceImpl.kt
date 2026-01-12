package com.koflox.cyclingassistant.data.source.asset

import android.content.Context
import com.koflox.cyclingassistant.data.source.asset.model.DestinationAsset
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class PoiAssetDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val context: Context,
) : PoiAssetDataSource {
    override suspend fun readDestinationsJson(): List<DestinationAsset> = withContext(dispatcherIo) {
        context.assets.open(DESTINATIONS_FILE).use { inputStream ->
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Json.decodeFromString<List<DestinationAsset>>(jsonString)
        }
    }

    companion object {
        private const val DESTINATIONS_FILE = "destinations.json"
    }
}
