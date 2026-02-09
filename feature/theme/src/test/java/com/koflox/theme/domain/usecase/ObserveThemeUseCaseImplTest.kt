package com.koflox.theme.domain.usecase

import app.cash.turbine.test
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.repository.ThemeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveThemeUseCaseImplTest {

    private val repository: ThemeRepository = mockk()
    private lateinit var useCase: ObserveThemeUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveThemeUseCaseImpl(repository = repository)
    }

    @Test
    fun `observeTheme delegates to repository`() = runTest {
        every { repository.observeTheme() } returns flowOf(AppTheme.DARK)

        useCase.observeTheme().test {
            assertEquals(AppTheme.DARK, awaitItem())
            awaitComplete()
        }

        verify { repository.observeTheme() }
    }

    @Test
    fun `observeTheme emits repository updates`() = runTest {
        every { repository.observeTheme() } returns flowOf(AppTheme.LIGHT, AppTheme.SYSTEM)

        useCase.observeTheme().test {
            assertEquals(AppTheme.LIGHT, awaitItem())
            assertEquals(AppTheme.SYSTEM, awaitItem())
            awaitComplete()
        }
    }
}
