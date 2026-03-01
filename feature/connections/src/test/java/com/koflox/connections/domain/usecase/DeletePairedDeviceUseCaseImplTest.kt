package com.koflox.connections.domain.usecase

import com.koflox.connections.domain.repository.PairedDeviceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeletePairedDeviceUseCaseImplTest {

    companion object {
        private const val DEVICE_ID = "device-123"
    }

    private val repository: PairedDeviceRepository = mockk()
    private lateinit var useCase: DeletePairedDeviceUseCaseImpl

    @Before
    fun setup() {
        useCase = DeletePairedDeviceUseCaseImpl(repository = repository)
    }

    @Test
    fun `delete delegates to repository`() = runTest {
        coEvery { repository.deleteDevice(DEVICE_ID) } returns Result.success(Unit)

        val result = useCase.delete(DEVICE_ID)

        assertTrue(result.isSuccess)
        coVerify { repository.deleteDevice(DEVICE_ID) }
    }

    @Test
    fun `delete returns failure when repository fails`() = runTest {
        coEvery { repository.deleteDevice(DEVICE_ID) } returns Result.failure(RuntimeException())

        val result = useCase.delete(DEVICE_ID)

        assertTrue(result.isFailure)
    }
}
