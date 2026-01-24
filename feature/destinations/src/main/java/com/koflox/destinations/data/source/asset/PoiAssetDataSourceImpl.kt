package com.koflox.destinations.data.source.asset

import android.content.Context
import com.koflox.destinations.data.source.asset.model.DestinationAsset
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class PoiAssetDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val context: Context,
) : PoiAssetDataSource {
    override suspend fun readDestinationsJson(fileName: String): List<DestinationAsset> =
        withContext(dispatcherIo) {
            context.assets.open(fileName).use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                Json.decodeFromString<List<DestinationAsset>>(jsonString)
            }
        }
}
