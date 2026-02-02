package com.koflox.location

import com.google.android.gms.location.LocationServices
import com.koflox.concurrent.DispatchersQualifier
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
}
