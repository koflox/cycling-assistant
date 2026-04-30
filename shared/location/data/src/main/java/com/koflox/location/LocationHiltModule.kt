package com.koflox.location

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.koflox.di.DefaultDispatcher
import com.koflox.di.IoDispatcher
import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.geolocation.LocationDataSourceImpl
import com.koflox.location.repository.UserLocationRepository
import com.koflox.location.repository.UserLocationRepositoryImpl
import com.koflox.location.settings.LocationSettingsDataSource
import com.koflox.location.settings.LocationSettingsDataSourceImpl
import com.koflox.location.smoother.KalmanLocationSmoother
import com.koflox.location.smoother.LocationSmoother
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.location.usecase.CheckLocationEnabledUseCaseImpl
import com.koflox.location.usecase.GetUserLocationUseCase
import com.koflox.location.usecase.GetUserLocationUseCaseImpl
import com.koflox.location.usecase.ObserveUserLocationUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCaseImpl
import com.koflox.location.validator.LocationValidator
import com.koflox.location.validator.LocationValidatorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocationHiltModule {

    @Provides
    @Singleton
    fun provideLocationDataSource(
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
        @ApplicationContext context: Context,
    ): LocationDataSource = LocationDataSourceImpl(
        dispatcherIo = dispatcherIo,
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context),
    )

    @Provides
    @Singleton
    fun provideLocationValidator(): LocationValidator = LocationValidatorImpl()

    @Provides
    fun provideLocationSmoother(): LocationSmoother = KalmanLocationSmoother()

    @Provides
    @Singleton
    fun provideLocationSettingsDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): LocationSettingsDataSource = LocationSettingsDataSourceImpl(
        context = context,
        dispatcherIo = dispatcherIo,
    )

    @Provides
    @Singleton
    fun provideUserLocationRepository(
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
        locationDataSource: LocationDataSource,
    ): UserLocationRepository = UserLocationRepositoryImpl(
        dispatcherDefault = dispatcherDefault,
        locationDataSource = locationDataSource,
    )

    @Provides
    fun provideGetUserLocationUseCase(
        repository: UserLocationRepository,
    ): GetUserLocationUseCase = GetUserLocationUseCaseImpl(
        repository = repository,
    )

    @Provides
    fun provideCheckLocationEnabledUseCase(
        locationSettingsDataSource: LocationSettingsDataSource,
    ): CheckLocationEnabledUseCase = CheckLocationEnabledUseCaseImpl(
        locationSettingsDataSource = locationSettingsDataSource,
    )

    @Provides
    fun provideObserveUserLocationUseCase(
        repository: UserLocationRepository,
    ): ObserveUserLocationUseCase = ObserveUserLocationUseCaseImpl(
        repository = repository,
    )
}
