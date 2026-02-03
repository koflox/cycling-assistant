package com.koflox.theme.domain.usecase

import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.repository.ThemeRepository

interface UpdateThemeUseCase {
    suspend fun updateTheme(theme: AppTheme)
}

internal class UpdateThemeUseCaseImpl(
    private val repository: ThemeRepository,
) : UpdateThemeUseCase {
    override suspend fun updateTheme(theme: AppTheme) = repository.setTheme(theme)
}
