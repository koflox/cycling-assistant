package com.koflox.profile.di

import com.koflox.profile.domain.repository.ProfileRepository
import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.profile.domain.usecase.GetRiderWeightUseCaseImpl
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCase
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ProfileDomainHiltModule {

    @Provides
    fun provideGetRiderWeightUseCase(
        repository: ProfileRepository,
    ): GetRiderWeightUseCase = GetRiderWeightUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateRiderWeightUseCase(
        repository: ProfileRepository,
    ): UpdateRiderWeightUseCase = UpdateRiderWeightUseCaseImpl(
        repository = repository,
    )
}
