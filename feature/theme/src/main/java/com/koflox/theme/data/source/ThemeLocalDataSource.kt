package com.koflox.theme.data.source

import com.koflox.theme.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

internal interface ThemeLocalDataSource {
    fun observeTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}
