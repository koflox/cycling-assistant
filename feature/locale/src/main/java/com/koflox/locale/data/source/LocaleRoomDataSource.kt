package com.koflox.locale.data.source

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.locale.data.source.local.dao.LocaleDao
import com.koflox.locale.data.source.local.entity.LocaleSettingsEntity
import com.koflox.locale.domain.model.AppLanguage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class LocaleRoomDataSource(
    private val daoFactory: ConcurrentFactory<LocaleDao>,
    private val dispatcherIo: CoroutineDispatcher,
) : LocaleLocalDataSource {

    override fun observeLanguage(): Flow<AppLanguage> = flow {
        emitAll(
            daoFactory.get().observeSettings()
                .map { entity ->
                    entity?.languageCode?.let { code ->
                        AppLanguage.entries.find { it.code == code }
                    } ?: AppLanguage.DEFAULT
                },
        )
    }.flowOn(dispatcherIo)

    override suspend fun setLanguage(language: AppLanguage) {
        withContext(dispatcherIo) {
            daoFactory.get().insertSettings(LocaleSettingsEntity(languageCode = language.code))
        }
    }
}
