package com.koflox.cyclingassistant.data.source.asset

import com.koflox.cyclingassistant.data.source.asset.model.DestinationAsset

internal interface PoiAssetDataSource {
    suspend fun readDestinationsJson(): List<DestinationAsset>
}
