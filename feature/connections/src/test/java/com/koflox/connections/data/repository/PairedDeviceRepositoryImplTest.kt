package com.koflox.connections.data.repository

import com.koflox.connections.data.mapper.PairedDeviceMapper
import com.koflox.connections.data.source.PairedDeviceLocalDataSource
import com.koflox.connections.data.source.local.entity.PairedDeviceEntity
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PairedDeviceRepositoryImplTest {

    companion object {
        private const val DEVICE_ID = "device-123"
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME = "4iiii PRECISION"
    }

    private val localDataSource: PairedDeviceLocalDataSource = mockk()
    private val mapper: PairedDeviceMapper = mockk()
    private lateinit var repository: PairedDeviceRepositoryImpl

    @Before
    fun setup() {
        repository = PairedDeviceRepositoryImpl(
            localDataSource = localDataSource,
            mapper = mapper,
        )
    }

    @Test
    fun `observeAllDevices maps entities to domain models`() = runTest {
        val entity = createEntity()
        val domain = createDomain()
        every { localDataSource.observeAllDevices() } returns flowOf(listOf(entity))
        every { mapper.toDomain(entity) } returns domain

        val result = repository.observeAllDevices().first()

        assertEquals(1, result.size)
        assertEquals(domain, result[0])
    }

    @Test
    fun `saveDevice maps domain to entity and inserts`() = runTest {
        val domain = createDomain()
        val entity = createEntity()
        every { mapper.toEntity(domain) } returns entity
        coEvery { localDataSource.insertDevice(entity) } returns Unit

        val result = repository.saveDevice(domain)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.insertDevice(entity) }
    }

    @Test
    fun `deleteDevice calls data source`() = runTest {
        coEvery { localDataSource.deleteDevice(DEVICE_ID) } returns Unit

        val result = repository.deleteDevice(DEVICE_ID)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.deleteDevice(DEVICE_ID) }
    }

    private fun createEntity(
        id: String = DEVICE_ID,
        macAddress: String = MAC_ADDRESS,
        name: String = DEVICE_NAME,
        deviceType: String = "POWER_METER",
        isSessionUsageEnabled: Boolean = false,
    ) = PairedDeviceEntity(
        id = id,
        macAddress = macAddress,
        name = name,
        deviceType = deviceType,
        isSessionUsageEnabled = isSessionUsageEnabled,
    )

    private fun createDomain(
        id: String = DEVICE_ID,
        macAddress: String = MAC_ADDRESS,
        name: String = DEVICE_NAME,
        deviceType: DeviceType = DeviceType.POWER_METER,
        isSessionUsageEnabled: Boolean = false,
    ) = PairedDevice(
        id = id,
        macAddress = macAddress,
        name = name,
        deviceType = deviceType,
        isSessionUsageEnabled = isSessionUsageEnabled,
    )
}
