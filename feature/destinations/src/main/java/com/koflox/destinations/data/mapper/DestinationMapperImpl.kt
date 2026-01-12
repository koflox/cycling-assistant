package com.koflox.destinations.data.mapper

import com.koflox.destinations.data.source.asset.model.DestinationAsset
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.destinations.domain.model.Destination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DestinationMapperImpl(
    private val dispatcherDefault: CoroutineDispatcher,
) : DestinationMapper {
    override suspend fun toDomain(entity: DestinationLocal): Destination = withContext(dispatcherDefault) {
        Destination(
            id = entity.id,
            title = entity.title,
            latitude = entity.latitude,
            longitude = entity.longitude,
        )
    }

    override suspend fun toEntity(json: DestinationAsset): DestinationLocal = withContext(dispatcherDefault) {
        DestinationLocal(
            id = json.id,
            title = json.title,
            latitude = json.latitude,
            longitude = json.longitude,
        )
    }

    override suspend fun toEntityList(jsonList: List<DestinationAsset>): List<DestinationLocal> = withContext(dispatcherDefault) {
        jsonList.map {
            toEntity(it)
        }
    }
}
