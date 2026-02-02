package com.koflox.settings.domain.usecase

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.repository.SettingsRepository

internal interface UpdateSettingsUseCase {
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateLanguage(language: AppLanguage)
    suspend fun updateRiderWeightKg(weightKg: Double)
}

internal class UpdateSettingsUseCaseImpl(
    private val repository: SettingsRepository,
) : UpdateSettingsUseCase {
    override suspend fun updateTheme(theme: AppTheme) = repository.setTheme(theme)
    override suspend fun updateLanguage(language: AppLanguage) = repository.setLanguage(language)
    override suspend fun updateRiderWeightKg(weightKg: Double) = repository.setRiderWeightKg(weightKg)
}
