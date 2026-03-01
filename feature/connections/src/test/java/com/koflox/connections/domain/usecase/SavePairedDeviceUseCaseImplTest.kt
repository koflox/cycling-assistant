package com.koflox.connections.domain.usecase

import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.repository.PairedDeviceRepository
import com.koflox.id.IdGenerator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SavePairedDeviceUseCaseImplTest {

    companion object {
        private const val GENERATED_ID = "generated-id"
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME = "4iiii PRECISION"
    }

    private val repository: PairedDeviceRepository = mockk()
    private val idGenerator: IdGenerator = mockk()
    private lateinit var useCase: SavePairedDeviceUseCaseImpl

    @Before
    fun setup() {
        every { idGenerator.generate() } returns GENERATED_ID
        every { repository.observeAllDevices() } returns flowOf(emptyList())
        coEvery { repository.saveDevice(any()) } returns Result.success(Unit)
        useCase = SavePairedDeviceUseCaseImpl(
            repository = repository,
            idGenerator = idGenerator,
        )
    }

    @Test
    fun `save creates device with generated id`() = runTest {
        val deviceSlot = slot<PairedDevice>()
        coEvery { repository.saveDevice(capture(deviceSlot)) } returns Result.success(Unit)

        useCase.save(MAC_ADDRESS, DEVICE_NAME, DeviceType.POWER_METER)

        assertEquals(GENERATED_ID, deviceSlot.captured.id)
        assertEquals(MAC_ADDRESS, deviceSlot.captured.macAddress)
        assertEquals(DEVICE_NAME, deviceSlot.captured.name)
        assertEquals(DeviceType.POWER_METER, deviceSlot.captured.deviceType)
        assertEquals(false, deviceSlot.captured.isSessionUsageEnabled)
    }

    @Test
    fun `save returns failure when device already paired`() = runTest {
        val existingDevice = PairedDevice(
            id = "existing-id",
            macAddress = MAC_ADDRESS,
            name = DEVICE_NAME,
            deviceType = DeviceType.POWER_METER,
            isSessionUsageEnabled = false,
        )
        every { repository.observeAllDevices() } returns flowOf(listOf(existingDevice))

        val result = useCase.save(MAC_ADDRESS, DEVICE_NAME, DeviceType.POWER_METER)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DeviceAlreadyPairedException)
    }

    @Test
    fun `save returns success when no duplicate`() = runTest {
        val result = useCase.save(MAC_ADDRESS, DEVICE_NAME, DeviceType.POWER_METER)

        assertTrue(result.isSuccess)
        coVerify { repository.saveDevice(any()) }
    }
}
