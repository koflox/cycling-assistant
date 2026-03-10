package com.koflox.connections.di

import com.koflox.connections.domain.repository.PairedDeviceRepository
import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCase
import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCaseImpl
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCaseImpl
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCase
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCaseImpl
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCase
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCaseImpl
import com.koflox.id.IdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ConnectionsDomainHiltModule {

    @Provides
    fun provideObservePairedDevicesUseCase(
        repository: PairedDeviceRepository,
    ): ObservePairedDevicesUseCase = ObservePairedDevicesUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideSavePairedDeviceUseCase(
        repository: PairedDeviceRepository,
        idGenerator: IdGenerator,
    ): SavePairedDeviceUseCase = SavePairedDeviceUseCaseImpl(
        repository = repository,
        idGenerator = idGenerator,
    )

    @Provides
    fun provideDeletePairedDeviceUseCase(
        repository: PairedDeviceRepository,
    ): DeletePairedDeviceUseCase = DeletePairedDeviceUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideUpdateDeviceSessionUsageUseCase(
        repository: PairedDeviceRepository,
    ): UpdateDeviceSessionUsageUseCase = UpdateDeviceSessionUsageUseCaseImpl(
        repository = repository,
    )
}
