package com.koflox.nutrition.di

import android.content.Context
import com.koflox.di.IoDispatcher
import com.koflox.nutrition.data.repository.NutritionSettingsRepositoryImpl
import com.koflox.nutrition.data.source.NutritionSettingsDataStore
import com.koflox.nutrition.data.source.NutritionSettingsLocalDataSource
import com.koflox.nutrition.domain.repository.NutritionSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NutritionDataHiltModule {

    @Provides
    @Singleton
    fun provideNutritionSettingsLocalDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): NutritionSettingsLocalDataSource = NutritionSettingsDataStore(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideNutritionSettingsRepository(
        localDataSource: NutritionSettingsLocalDataSource,
    ): NutritionSettingsRepository = NutritionSettingsRepositoryImpl(
        localDataSource = localDataSource,
    )
}
