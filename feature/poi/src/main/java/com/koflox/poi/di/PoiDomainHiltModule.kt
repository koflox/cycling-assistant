package com.koflox.poi.di

import com.koflox.poi.domain.repository.PoiRepository
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCase
import com.koflox.poi.domain.usecase.ObserveSelectedPoisUseCaseImpl
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCase
import com.koflox.poi.domain.usecase.UpdateSelectedPoisUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object PoiDomainHiltModule {

    @Provides
    fun provideObserveSelectedPoisUseCase(
        repository: PoiRepository,
    ): ObserveSelectedPoisUseCase = ObserveSelectedPoisUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateSelectedPoisUseCase(
        repository: PoiRepository,
    ): UpdateSelectedPoisUseCase = UpdateSelectedPoisUseCaseImpl(
        repository = repository,
    )
}
