package com.koflox.location

import com.google.android.gms.location.LocationServices
import com.koflox.concurrent.DispatchersQualifier
import com.koflox.location.geolocation.LocationDataSource
import com.koflox.location.geolocation.LocationDataSourceImpl
import com.koflox.location.repository.UserLocationRepository
import com.koflox.location.repository.UserLocationRepositoryImpl
import com.koflox.location.settings.LocationSettingsDataSource
import com.koflox.location.settings.LocationSettingsDataSourceImpl
import com.koflox.location.smoother.KalmanLocationSmoother
import com.koflox.location.smoother.LocationSmoother
import com.koflox.location.usecase.GetUserLocationUseCase
import com.koflox.location.usecase.GetUserLocationUseCaseImpl
import com.koflox.location.usecase.ObserveUserLocationUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCaseImpl
import com.koflox.location.validator.LocationValidator
import com.koflox.location.validator.LocationValidatorImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val locationModule = module {
    single<LocationDataSource> {
        LocationDataSourceImpl(
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(androidContext()),
        )
    }
    single<LocationValidator> {
        LocationValidatorImpl()
    }
    factory<LocationSmoother> {
        KalmanLocationSmoother()
    }
    single<LocationSettingsDataSource> {
        LocationSettingsDataSourceImpl(
            context = androidContext(),
            dispatcherIo = get<CoroutineDispatcher>(DispatchersQualifier.Io),
        )
    }
    single<UserLocationRepository> {
        UserLocationRepositoryImpl(
            dispatcherDefault = get<CoroutineDispatcher>(DispatchersQualifier.Default),
            locationDataSource = get(),
        )
    }
    factory<GetUserLocationUseCase> {
        GetUserLocationUseCaseImpl(
            repository = get(),
        )
    }
    factory<ObserveUserLocationUseCase> {
        ObserveUserLocationUseCaseImpl(
            repository = get(),
        )
    }
}
