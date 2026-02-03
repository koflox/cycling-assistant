package com.koflox.locale.data.repository

import com.koflox.locale.data.source.LocaleLocalDataSource
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.repository.LocaleRepository
import kotlinx.coroutines.flow.Flow

internal class LocaleRepositoryImpl(
    private val localDataSource: LocaleLocalDataSource,
) : LocaleRepository {
    override fun observeLanguage(): Flow<AppLanguage> = localDataSource.observeLanguage()

    override suspend fun setLanguage(language: AppLanguage) {
        localDataSource.setLanguage(language)
    }
}
