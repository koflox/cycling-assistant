package com.koflox.destinations.data.mapper

import com.koflox.destinations.data.source.asset.model.DestinationAsset
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.destinations.domain.model.Destination

internal interface DestinationMapper {
    suspend fun toDomain(entity: DestinationLocal): Destination
    suspend fun toEntity(json: DestinationAsset): DestinationLocal
    suspend fun toEntityList(jsonList: List<DestinationAsset>): List<DestinationLocal>
}
