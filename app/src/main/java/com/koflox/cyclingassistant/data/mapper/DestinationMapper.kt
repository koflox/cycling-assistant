package com.koflox.cyclingassistant.data.mapper

import com.koflox.cyclingassistant.data.source.asset.model.DestinationAsset
import com.koflox.cyclingassistant.data.source.local.entity.DestinationLocal
import com.koflox.cyclingassistant.domain.model.Destination

internal interface DestinationMapper {
    suspend fun toDomain(entity: DestinationLocal): Destination
    suspend fun toEntity(json: DestinationAsset): DestinationLocal
    suspend fun toEntityList(jsonList: List<DestinationAsset>): List<DestinationLocal>
}
