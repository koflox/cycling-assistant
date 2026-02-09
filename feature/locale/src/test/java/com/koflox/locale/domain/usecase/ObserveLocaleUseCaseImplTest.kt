package com.koflox.locale.domain.usecase

import app.cash.turbine.test
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.repository.LocaleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveLocaleUseCaseImplTest {

    private val repository: LocaleRepository = mockk()
    private lateinit var useCase: ObserveLocaleUseCaseImpl

    @Before
    fun setup() {
        useCase = ObserveLocaleUseCaseImpl(repository = repository)
    }

    @Test
    fun `observeLanguage delegates to repository`() = runTest {
        every { repository.observeLanguage() } returns flowOf(AppLanguage.ENGLISH)

        useCase.observeLanguage().test {
            assertEquals(AppLanguage.ENGLISH, awaitItem())
            awaitComplete()
        }

        verify { repository.observeLanguage() }
    }

    @Test
    fun `observeLanguage emits repository updates`() = runTest {
        every { repository.observeLanguage() } returns flowOf(AppLanguage.ENGLISH, AppLanguage.JAPANESE)

        useCase.observeLanguage().test {
            assertEquals(AppLanguage.ENGLISH, awaitItem())
            assertEquals(AppLanguage.JAPANESE, awaitItem())
            awaitComplete()
        }
    }
}
