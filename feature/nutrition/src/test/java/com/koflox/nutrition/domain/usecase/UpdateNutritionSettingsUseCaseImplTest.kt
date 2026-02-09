package com.koflox.nutrition.domain.usecase

import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateNutritionSettingsUseCaseImplTest {

    companion object {
        private const val INTERVAL_MINUTES = 30
    }

    private val repository: NutritionSettingsRepository = mockk()
    private lateinit var useCase: UpdateNutritionSettingsUseCaseImpl

    @Before
    fun setup() {
        coEvery { repository.setEnabled(any()) } returns Unit
        coEvery { repository.setIntervalMinutes(any()) } returns Unit
        useCase = UpdateNutritionSettingsUseCaseImpl(repository = repository)
    }

    @Test
    fun `setEnabled delegates true to repository`() = runTest {
        useCase.setEnabled(true)

        coVerify { repository.setEnabled(true) }
    }

    @Test
    fun `setEnabled delegates false to repository`() = runTest {
        useCase.setEnabled(false)

        coVerify { repository.setEnabled(false) }
    }

    @Test
    fun `setIntervalMinutes delegates to repository`() = runTest {
        useCase.setIntervalMinutes(INTERVAL_MINUTES)

        coVerify { repository.setIntervalMinutes(INTERVAL_MINUTES) }
    }
}
