package com.koflox.theme.data.repository

import com.koflox.theme.data.source.ThemeLocalDataSource
import com.koflox.theme.domain.model.AppTheme
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ThemeRepositoryImplTest {

    private val localDataSource: ThemeLocalDataSource = mockk(relaxUnitFun = true)
    private lateinit var repository: ThemeRepositoryImpl

    @Before
    fun setup() {
        repository = ThemeRepositoryImpl(localDataSource)
    }

    @Test
    fun `observeTheme delegates to data source`() = runTest {
        every { localDataSource.observeTheme() } returns flowOf(AppTheme.DARK)

        val result = repository.observeTheme().first()

        assertEquals(AppTheme.DARK, result)
    }

    @Test
    fun `setTheme delegates to data source`() = runTest {
        repository.setTheme(AppTheme.LIGHT)

        coVerify { localDataSource.setTheme(AppTheme.LIGHT) }
    }
}
