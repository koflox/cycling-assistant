package com.koflox.connections.data.mapper

import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import com.koflox.connections.domain.model.PairedDevice

internal interface PairedDeviceMapper {
    fun toDomain(entity: PairedDeviceEntity): PairedDevice
    fun toEntity(domain: PairedDevice): PairedDeviceEntity
}
