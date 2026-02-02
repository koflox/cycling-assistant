package com.koflox.settings.domain.usecase

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.model.InvalidWeightException
import com.koflox.settings.domain.repository.SettingsRepository

internal interface UpdateSettingsUseCase {
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateLanguage(language: AppLanguage)
    suspend fun updateRiderWeightKg(weightKg: String): Result<Unit>
}

internal class UpdateSettingsUseCaseImpl(
    private val repository: SettingsRepository,
) : UpdateSettingsUseCase {

    companion object {
        private const val MIN_WEIGHT_KG = 1.0
        private const val MAX_WEIGHT_KG = 300.0
    }

    override suspend fun updateTheme(theme: AppTheme) = repository.setTheme(theme)
    override suspend fun updateLanguage(language: AppLanguage) = repository.setLanguage(language)

    override suspend fun updateRiderWeightKg(weightKg: String): Result<Unit> {
        val parsed = parseAndValidateWeight(weightKg)
            ?: return Result.failure(InvalidWeightException(MIN_WEIGHT_KG, MAX_WEIGHT_KG))
        repository.setRiderWeightKg(parsed)
        return Result.success(Unit)
    }

    private fun parseAndValidateWeight(weightKg: String): Double? {
        val parsed = weightKg.toDoubleOrNull() ?: return null
        return parsed.takeIf { it in MIN_WEIGHT_KG..MAX_WEIGHT_KG }
    }
}
