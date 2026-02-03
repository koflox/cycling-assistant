package com.koflox.locale.data.source

import com.koflox.locale.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

internal interface LocaleLocalDataSource {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}
