package com.koflox.settings.domain.usecase

import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

internal interface ObserveSettingsUseCase {
    fun observeTheme(): Flow<AppTheme>
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun getRiderWeightKg(): Float
}

internal class ObserveSettingsUseCaseImpl(
    private val repository: SettingsRepository,
) : ObserveSettingsUseCase {
    override fun observeTheme(): Flow<AppTheme> = repository.observeTheme()
    override fun observeLanguage(): Flow<AppLanguage> = repository.observeLanguage()
    override suspend fun getRiderWeightKg(): Float = repository.getRiderWeightKg()
}
