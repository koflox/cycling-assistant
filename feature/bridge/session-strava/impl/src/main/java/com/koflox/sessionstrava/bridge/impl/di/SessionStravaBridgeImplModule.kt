package com.koflox.sessionstrava.bridge.impl.di

import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.sessionstrava.bridge.SessionGpxDataProvider
import com.koflox.sessionstrava.bridge.impl.SessionGpxDataProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SessionStravaBridgeImplModule {

    @Provides
    @Singleton
    fun provideSessionGpxDataProvider(
        getSessionByIdUseCase: GetSessionByIdUseCase,
    ): SessionGpxDataProvider = SessionGpxDataProviderImpl(
        getSessionByIdUseCase = getSessionByIdUseCase,
    )
}
