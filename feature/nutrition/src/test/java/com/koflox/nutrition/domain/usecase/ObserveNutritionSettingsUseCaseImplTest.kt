package com.koflox.nutrition.domain.usecase

import app.cash.turbine.test
import com.koflox.nutrition.domain.model.NutritionSettings
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveNutritionSettingsUseCaseImplTest {

    companion object {
        private const val INTERVAL_MINUTES = 25
        private const val CUSTOM_INTERVAL_MINUTES = 30
    }

    private val repository: NutritionSettingsRepository = mockk()
    private lateinit var useCase: ObserveNutritionSettingsUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveNutritionSettingsUseCaseImpl(repository = repository)
    }

    @Test
    fun `observeSettings delegates to repository`() = runTest {
        val settings = NutritionSettings(isEnabled = true, intervalMinutes = INTERVAL_MINUTES)
        every { repository.observeSettings() } returns flowOf(settings)

        useCase.observeSettings().test {
            assertEquals(settings, awaitItem())
            awaitComplete()
        }

        verify { repository.observeSettings() }
    }

    @Test
    fun `observeSettings emits repository updates`() = runTest {
        val settings1 = NutritionSettings(isEnabled = true, intervalMinutes = INTERVAL_MINUTES)
        val settings2 = NutritionSettings(isEnabled = false, intervalMinutes = CUSTOM_INTERVAL_MINUTES)
        every { repository.observeSettings() } returns flowOf(settings1, settings2)

        useCase.observeSettings().test {
            assertEquals(settings1, awaitItem())
            assertEquals(settings2, awaitItem())
            awaitComplete()
        }
    }
}
