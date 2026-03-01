package com.koflox.connections.data.mapper

import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class PairedDeviceMapperImplTest {

    companion object {
        private const val DEVICE_ID = "device-123"
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME = "4iiii PRECISION"
        private const val DEVICE_TYPE_STRING = "POWER_METER"
    }

    private val mapper: PairedDeviceMapper = PairedDeviceMapperImpl()

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = PairedDeviceEntity(
            id = DEVICE_ID,
            macAddress = MAC_ADDRESS,
            name = DEVICE_NAME,
            deviceType = DEVICE_TYPE_STRING,
            isSessionUsageEnabled = true,
        )
        val result = mapper.toDomain(entity)
        assertEquals(DEVICE_ID, result.id)
        assertEquals(MAC_ADDRESS, result.macAddress)
        assertEquals(DEVICE_NAME, result.name)
        assertEquals(DeviceType.POWER_METER, result.deviceType)
        assertEquals(true, result.isSessionUsageEnabled)
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = PairedDevice(
            id = DEVICE_ID,
            macAddress = MAC_ADDRESS,
            name = DEVICE_NAME,
            deviceType = DeviceType.POWER_METER,
            isSessionUsageEnabled = false,
        )
        val result = mapper.toEntity(domain)
        assertEquals(DEVICE_ID, result.id)
        assertEquals(MAC_ADDRESS, result.macAddress)
        assertEquals(DEVICE_NAME, result.name)
        assertEquals(DEVICE_TYPE_STRING, result.deviceType)
        assertEquals(false, result.isSessionUsageEnabled)
    }
}
