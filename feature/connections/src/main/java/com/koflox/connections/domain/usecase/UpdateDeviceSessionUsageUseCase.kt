package com.koflox.connections.domain.usecase

import com.koflox.concurrent.suspendMapCatching
import com.koflox.connections.domain.repository.PairedDeviceRepository

interface UpdateDeviceSessionUsageUseCase {
    suspend fun update(id: String, isEnabled: Boolean): Result<Unit>
}

internal class UpdateDeviceSessionUsageUseCaseImpl(
    private val repository: PairedDeviceRepository,
) : UpdateDeviceSessionUsageUseCase {

    override suspend fun update(id: String, isEnabled: Boolean): Result<Unit> =
        repository.getDeviceById(id).suspendMapCatching { device ->
            requireNotNull(device) { "Device with id=$id not found" }
            repository.updateDevice(device.copy(isSessionUsageEnabled = isEnabled)).getOrThrow()
        }
}
