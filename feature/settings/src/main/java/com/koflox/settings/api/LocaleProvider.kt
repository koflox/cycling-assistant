package com.koflox.settings.api

import com.koflox.settings.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface LocaleProvider {
    fun observeLanguage(): Flow<AppLanguage>
}
