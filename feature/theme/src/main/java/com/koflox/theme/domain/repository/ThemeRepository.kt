package com.koflox.theme.domain.repository

import com.koflox.theme.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

internal interface ThemeRepository {
    fun observeTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}
