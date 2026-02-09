package com.koflox.locale.domain.usecase

import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.repository.LocaleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateLocaleUseCaseImplTest {

    private val repository: LocaleRepository = mockk()
    private lateinit var useCase: UpdateLocaleUseCaseImpl

    @Before
    fun setup() {
        coEvery { repository.setLanguage(any()) } returns Unit
        useCase = UpdateLocaleUseCaseImpl(repository = repository)
    }

    @Test
    fun `updateLanguage delegates ENGLISH to repository`() = runTest {
        useCase.updateLanguage(AppLanguage.ENGLISH)

        coVerify { repository.setLanguage(AppLanguage.ENGLISH) }
    }

    @Test
    fun `updateLanguage delegates RUSSIAN to repository`() = runTest {
        useCase.updateLanguage(AppLanguage.RUSSIAN)

        coVerify { repository.setLanguage(AppLanguage.RUSSIAN) }
    }

    @Test
    fun `updateLanguage delegates JAPANESE to repository`() = runTest {
        useCase.updateLanguage(AppLanguage.JAPANESE)

        coVerify { repository.setLanguage(AppLanguage.JAPANESE) }
    }
}
