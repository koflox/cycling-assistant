package com.koflox.destinations.domain.usecase

import com.koflox.destinations.domain.model.RidingMode
import com.koflox.destinations.domain.repository.RidePreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateRidingModeUseCaseTest {

    private val repository: RidePreferencesRepository = mockk()
    private lateinit var useCase: UpdateRidingModeUseCaseImpl

    @Before
    fun setup() {
        useCase = UpdateRidingModeUseCaseImpl(repository = repository)
    }

    @Test
    fun `update delegates FREE_ROAM to repository`() = runTest {
        coEvery { repository.setRidingMode(any()) } returns Unit

        useCase.update(RidingMode.FREE_ROAM)

        coVerify(exactly = 1) { repository.setRidingMode(RidingMode.FREE_ROAM) }
    }

    @Test
    fun `update delegates DESTINATION to repository`() = runTest {
        coEvery { repository.setRidingMode(any()) } returns Unit

        useCase.update(RidingMode.DESTINATION)

        coVerify(exactly = 1) { repository.setRidingMode(RidingMode.DESTINATION) }
    }
}
