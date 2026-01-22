package com.koflox.settings.api

import com.koflox.settings.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface ThemeProvider {
    fun observeTheme(): Flow<AppTheme>
}
