package com.koflox.connections.domain.usecase

import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.repository.PairedDeviceRepository
import com.koflox.id.IdGenerator
import kotlinx.coroutines.flow.first

interface SavePairedDeviceUseCase {
    suspend fun save(macAddress: String, name: String, deviceType: DeviceType): Result<Unit>
}

class DeviceAlreadyPairedException(macAddress: String) :
    IllegalStateException("Device with address $macAddress is already paired")

internal class SavePairedDeviceUseCaseImpl(
    private val repository: PairedDeviceRepository,
    private val idGenerator: IdGenerator,
) : SavePairedDeviceUseCase {

    override suspend fun save(macAddress: String, name: String, deviceType: DeviceType): Result<Unit> {
        val existingDevices = repository.observeAllDevices().first()
        if (existingDevices.any { it.macAddress == macAddress }) {
            return Result.failure(DeviceAlreadyPairedException(macAddress))
        }
        val device = PairedDevice(
            id = idGenerator.generate(),
            macAddress = macAddress,
            name = name,
            deviceType = deviceType,
            isSessionUsageEnabled = false,
        )
        return repository.saveDevice(device)
    }
}
