package com.koflox.profilesession.bridge.impl.di

import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.profilesession.bridge.api.RiderProfileUseCase
import com.koflox.profilesession.bridge.impl.usecase.RiderProfileUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object BridgeImplModule {

    @Provides
    fun provideRiderProfileUseCase(
        getRiderWeightUseCase: GetRiderWeightUseCase,
    ): RiderProfileUseCase = RiderProfileUseCaseImpl(
        getRiderWeightUseCase = getRiderWeightUseCase,
    )
}
