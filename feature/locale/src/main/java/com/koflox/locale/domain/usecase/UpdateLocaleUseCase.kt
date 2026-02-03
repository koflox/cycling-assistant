package com.koflox.locale.domain.usecase

import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.repository.LocaleRepository

interface UpdateLocaleUseCase {
    suspend fun updateLanguage(language: AppLanguage)
}

internal class UpdateLocaleUseCaseImpl(
    private val repository: LocaleRepository,
) : UpdateLocaleUseCase {
    override suspend fun updateLanguage(language: AppLanguage) = repository.setLanguage(language)
}
