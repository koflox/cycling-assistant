package com.koflox.locale.di

import com.koflox.locale.domain.repository.LocaleRepository
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.locale.domain.usecase.ObserveLocaleUseCaseImpl
import com.koflox.locale.domain.usecase.UpdateLocaleUseCase
import com.koflox.locale.domain.usecase.UpdateLocaleUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object LocaleDomainHiltModule {

    @Provides
    fun provideObserveLocaleUseCase(
        repository: LocaleRepository,
    ): ObserveLocaleUseCase = ObserveLocaleUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateLocaleUseCase(
        repository: LocaleRepository,
    ): UpdateLocaleUseCase = UpdateLocaleUseCaseImpl(
        repository = repository,
    )
}
