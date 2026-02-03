package com.koflox.locale.domain.usecase

import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.repository.LocaleRepository
import kotlinx.coroutines.flow.Flow

interface ObserveLocaleUseCase {
    fun observeLanguage(): Flow<AppLanguage>
}

internal class ObserveLocaleUseCaseImpl(
    private val repository: LocaleRepository,
) : ObserveLocaleUseCase {
    override fun observeLanguage(): Flow<AppLanguage> = repository.observeLanguage()
}
