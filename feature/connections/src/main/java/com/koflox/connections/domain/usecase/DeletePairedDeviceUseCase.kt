package com.koflox.connections.domain.usecase

import com.koflox.connections.domain.repository.PairedDeviceRepository

interface DeletePairedDeviceUseCase {
    suspend fun delete(id: String): Result<Unit>
}

internal class DeletePairedDeviceUseCaseImpl(
    private val repository: PairedDeviceRepository,
) : DeletePairedDeviceUseCase {

    override suspend fun delete(id: String): Result<Unit> = repository.deleteDevice(id)
}
