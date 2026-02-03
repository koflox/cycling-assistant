package com.koflox.nutrition.di

import com.koflox.concurrent.DispatchersQualifier
import com.koflox.nutrition.data.repository.NutritionSettingsRepositoryImpl
import com.koflox.nutrition.data.source.NutritionSettingsDataStore
import com.koflox.nutrition.data.source.NutritionSettingsLocalDataSource
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val dataSourceModule = module {
    single<NutritionSettingsLocalDataSource> {
        NutritionSettingsDataStore(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
}

private val repoModule = module {
    single<NutritionSettingsRepository> {
        NutritionSettingsRepositoryImpl(localDataSource = get())
    }
}

internal val dataModules = listOf(dataSourceModule, repoModule)
