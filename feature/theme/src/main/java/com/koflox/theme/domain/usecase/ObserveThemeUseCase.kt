package com.koflox.theme.domain.usecase

import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

interface ObserveThemeUseCase {
    fun observeTheme(): Flow<AppTheme>
}

internal class ObserveThemeUseCaseImpl(
    private val repository: ThemeRepository,
) : ObserveThemeUseCase {
    override fun observeTheme(): Flow<AppTheme> = repository.observeTheme()
}
