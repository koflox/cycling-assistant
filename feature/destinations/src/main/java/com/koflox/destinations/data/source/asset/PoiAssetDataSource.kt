package com.koflox.destinations.data.source.asset

import com.koflox.destinations.data.source.asset.model.DestinationAsset

internal interface PoiAssetDataSource {
    suspend fun readDestinationsJson(fileName: String): List<DestinationAsset>
}
