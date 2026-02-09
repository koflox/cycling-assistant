package com.koflox.nutrition.data.repository

import app.cash.turbine.test
import com.koflox.nutrition.data.source.NutritionSettingsLocalDataSource
import com.koflox.nutrition.domain.model.NutritionSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NutritionSettingsRepositoryImplTest {

    companion object {
        private const val INTERVAL_MINUTES = 25
    }

    private val localDataSource: NutritionSettingsLocalDataSource = mockk()
    private lateinit var repository: NutritionSettingsRepositoryImpl

    @Before
    fun setup() {
        coEvery { localDataSource.setEnabled(any()) } returns Unit
        coEvery { localDataSource.setIntervalMinutes(any()) } returns Unit
        repository = NutritionSettingsRepositoryImpl(localDataSource = localDataSource)
    }

    @Test
    fun `observeSettings delegates to data source`() = runTest {
        val settings = NutritionSettings(isEnabled = true, intervalMinutes = INTERVAL_MINUTES)
        every { localDataSource.observeSettings() } returns flowOf(settings)

        repository.observeSettings().test {
            assertEquals(settings, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `setEnabled delegates to data source`() = runTest {
        repository.setEnabled(true)

        coVerify { localDataSource.setEnabled(true) }
    }

    @Test
    fun `setIntervalMinutes delegates to data source`() = runTest {
        repository.setIntervalMinutes(INTERVAL_MINUTES)

        coVerify { localDataSource.setIntervalMinutes(INTERVAL_MINUTES) }
    }
}
