package com.koflox.locale.data.repository

import com.koflox.locale.data.source.LocaleLocalDataSource
import com.koflox.locale.domain.model.AppLanguage
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocaleRepositoryImplTest {

    private val localDataSource: LocaleLocalDataSource = mockk(relaxUnitFun = true)
    private lateinit var repository: LocaleRepositoryImpl

    @Before
    fun setup() {
        repository = LocaleRepositoryImpl(localDataSource)
    }

    @Test
    fun `observeLanguage delegates to data source`() = runTest {
        every { localDataSource.observeLanguage() } returns flowOf(AppLanguage.JAPANESE)

        val result = repository.observeLanguage().first()

        assertEquals(AppLanguage.JAPANESE, result)
    }

    @Test
    fun `setLanguage delegates to data source`() = runTest {
        repository.setLanguage(AppLanguage.RUSSIAN)

        coVerify { localDataSource.setLanguage(AppLanguage.RUSSIAN) }
    }
}
