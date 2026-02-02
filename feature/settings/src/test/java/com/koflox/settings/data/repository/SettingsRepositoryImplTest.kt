package com.koflox.settings.data.repository

import com.koflox.settings.data.source.SettingsLocalDataSource
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    companion object {
        private const val RIDER_WEIGHT_KG = 75.0f
        private const val NEW_WEIGHT_KG = 80.0
    }

    private val localDataSource: SettingsLocalDataSource = mockk(relaxUnitFun = true)
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        repository = SettingsRepositoryImpl(localDataSource)
    }

    @Test
    fun `observeTheme delegates to data source`() = runTest {
        every { localDataSource.observeTheme() } returns flowOf(AppTheme.DARK)

        val result = repository.observeTheme().first()

        assertEquals(AppTheme.DARK, result)
    }

    @Test
    fun `observeLanguage delegates to data source`() = runTest {
        every { localDataSource.observeLanguage() } returns flowOf(AppLanguage.JAPANESE)

        val result = repository.observeLanguage().first()

        assertEquals(AppLanguage.JAPANESE, result)
    }

    @Test
    fun `getRiderWeightKg delegates to data source`() = runTest {
        coEvery { localDataSource.getRiderWeightKg() } returns RIDER_WEIGHT_KG

        val result = repository.getRiderWeightKg()

        assertEquals(RIDER_WEIGHT_KG, result)
    }

    @Test
    fun `getRiderWeightKg returns null when not set`() = runTest {
        coEvery { localDataSource.getRiderWeightKg() } returns null

        val result = repository.getRiderWeightKg()

        assertNull(result)
    }

    @Test
    fun `setTheme delegates to data source`() = runTest {
        repository.setTheme(AppTheme.LIGHT)

        coVerify { localDataSource.setTheme(AppTheme.LIGHT) }
    }

    @Test
    fun `setLanguage delegates to data source`() = runTest {
        repository.setLanguage(AppLanguage.RUSSIAN)

        coVerify { localDataSource.setLanguage(AppLanguage.RUSSIAN) }
    }

    @Test
    fun `setRiderWeightKg delegates to data source`() = runTest {
        repository.setRiderWeightKg(NEW_WEIGHT_KG)

        coVerify { localDataSource.setRiderWeightKg(NEW_WEIGHT_KG) }
    }
}
