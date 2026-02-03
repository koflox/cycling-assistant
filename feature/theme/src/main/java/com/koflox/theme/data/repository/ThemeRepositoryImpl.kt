package com.koflox.theme.data.repository

import com.koflox.theme.data.source.ThemeLocalDataSource
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

internal class ThemeRepositoryImpl(
    private val localDataSource: ThemeLocalDataSource,
) : ThemeRepository {
    override fun observeTheme(): Flow<AppTheme> = localDataSource.observeTheme()

    override suspend fun setTheme(theme: AppTheme) {
        localDataSource.setTheme(theme)
    }
}
