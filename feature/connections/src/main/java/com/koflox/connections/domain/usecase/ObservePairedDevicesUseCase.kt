package com.koflox.connections.domain.usecase

import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.repository.PairedDeviceRepository
import kotlinx.coroutines.flow.Flow

interface ObservePairedDevicesUseCase {
    fun observeAll(): Flow<List<PairedDevice>>
}

internal class ObservePairedDevicesUseCaseImpl(
    private val repository: PairedDeviceRepository,
) : ObservePairedDevicesUseCase {

    override fun observeAll(): Flow<List<PairedDevice>> = repository.observeAllDevices()
}
