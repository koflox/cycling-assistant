package com.koflox.theme.di

import com.koflox.theme.domain.repository.ThemeRepository
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import com.koflox.theme.domain.usecase.ObserveThemeUseCaseImpl
import com.koflox.theme.domain.usecase.UpdateThemeUseCase
import com.koflox.theme.domain.usecase.UpdateThemeUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ThemeDomainHiltModule {

    @Provides
    fun provideObserveThemeUseCase(
        repository: ThemeRepository,
    ): ObserveThemeUseCase = ObserveThemeUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateThemeUseCase(
        repository: ThemeRepository,
    ): UpdateThemeUseCase = UpdateThemeUseCaseImpl(
        repository = repository,
    )
}
