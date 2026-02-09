package com.koflox.theme.domain.usecase

import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.repository.ThemeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateThemeUseCaseImplTest {

    private val repository: ThemeRepository = mockk()
    private lateinit var useCase: UpdateThemeUseCaseImpl

    @Before
    fun setup() {
        coEvery { repository.setTheme(any()) } returns Unit
        useCase = UpdateThemeUseCaseImpl(repository = repository)
    }

    @Test
    fun `updateTheme delegates LIGHT to repository`() = runTest {
        useCase.updateTheme(AppTheme.LIGHT)

        coVerify { repository.setTheme(AppTheme.LIGHT) }
    }

    @Test
    fun `updateTheme delegates DARK to repository`() = runTest {
        useCase.updateTheme(AppTheme.DARK)

        coVerify { repository.setTheme(AppTheme.DARK) }
    }

    @Test
    fun `updateTheme delegates SYSTEM to repository`() = runTest {
        useCase.updateTheme(AppTheme.SYSTEM)

        coVerify { repository.setTheme(AppTheme.SYSTEM) }
    }
}
