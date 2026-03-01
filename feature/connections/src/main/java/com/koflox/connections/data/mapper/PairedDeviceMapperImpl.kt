package com.koflox.connections.data.mapper

import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice

internal class PairedDeviceMapperImpl : PairedDeviceMapper {

    override fun toDomain(entity: PairedDeviceEntity): PairedDevice = PairedDevice(
        id = entity.id,
        macAddress = entity.macAddress,
        name = entity.name,
        deviceType = DeviceType.valueOf(entity.deviceType),
        isSessionUsageEnabled = entity.isSessionUsageEnabled,
    )

    override fun toEntity(domain: PairedDevice): PairedDeviceEntity = PairedDeviceEntity(
        id = domain.id,
        macAddress = domain.macAddress,
        name = domain.name,
        deviceType = domain.deviceType.name,
        isSessionUsageEnabled = domain.isSessionUsageEnabled,
    )
}
