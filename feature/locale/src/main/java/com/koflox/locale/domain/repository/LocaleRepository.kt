package com.koflox.locale.domain.repository

import com.koflox.locale.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

internal interface LocaleRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}
