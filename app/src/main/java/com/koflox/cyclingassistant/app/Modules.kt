package com.koflox.cyclingassistant.app

import com.koflox.altitude.di.altitudeModule
import com.koflox.ble.di.bleModule
import com.koflox.concurrent.ConcurrentFactory
import com.koflox.concurrent.DispatchersQualifier
import com.koflox.concurrent.concurrentModule
import com.koflox.connections.di.ConnectionsQualifier
import com.koflox.connections.di.connectionsModule
import com.koflox.connectionsession.bridge.impl.di.connectionSessionBridgeImplModule
import com.koflox.cyclingassistant.BuildConfig
import com.koflox.cyclingassistant.MainViewModel
import com.koflox.cyclingassistant.data.AppDatabase
import com.koflox.cyclingassistant.data.DatabasePassphraseManager
import com.koflox.cyclingassistant.data.DatabasePassphraseManagerImpl
import com.koflox.cyclingassistant.data.RoomDatabaseFactory
import com.koflox.cyclingassistant.locale.LocalizedContextProviderImpl
import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.destinationnutrition.bridge.impl.di.destinationNutritionBridgeImplModule
import com.koflox.destinationpoi.bridge.impl.di.destinationPoiBridgeImplModule
import com.koflox.destinations.di.DestinationsQualifier
import com.koflox.destinations.di.destinationsModule
import com.koflox.destinationsession.bridge.impl.di.destinationSessionBridgeImplModule
import com.koflox.distance.di.distanceModule
import com.koflox.error.di.errorMapperModule
import com.koflox.id.di.idModule
import com.koflox.locale.di.LocaleQualifier
import com.koflox.locale.di.localeModule
import com.koflox.location.locationModule
import com.koflox.map.di.mapModule
import com.koflox.nutrition.di.nutritionModule
import com.koflox.nutritionsession.bridge.impl.di.nutritionSessionBridgeImplModule
import com.koflox.nutritionsettings.bridge.impl.di.nutritionSettingsBridgeImplModule
import com.koflox.poi.di.poiModule
import com.koflox.poisettings.bridge.impl.di.poiSettingsBridgeImplModule
import com.koflox.profile.di.ProfileQualifier
import com.koflox.profile.di.profileModule
import com.koflox.profilesession.bridge.impl.di.profileSessionBridgeImplModule
import com.koflox.sensor.power.di.powerSensorModule
import com.koflox.session.di.SessionQualifier
import com.koflox.session.di.sessionModule
import com.koflox.sessionsettings.bridge.impl.di.sessionSettingsBridgeImplModule
import com.koflox.settings.di.settingsModule
import com.koflox.theme.di.themeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val mainModule = module {
    single<LocalizedContextProvider> {
        LocalizedContextProviderImpl(
            applicationContext = androidContext(),
            observeLocaleUseCase = get(),
            dispatcherDefault = get(DispatchersQualifier.Default),
        )
    }
    viewModel {
        MainViewModel(
            observeThemeUseCase = get(),
            observeLocaleUseCase = get(),
            pendingSessionAction = get(),
        )
    }
}

private val databaseModule = module {
    single<DatabasePassphraseManager> { DatabasePassphraseManagerImpl(context = androidContext()) }
    single<ConcurrentFactory<AppDatabase>> {
        RoomDatabaseFactory(
            context = androidContext(),
            passphraseManager = get(),
            dispatcherIo = get(DispatchersQualifier.Io),
            isEncryptionEnabled = !BuildConfig.DEBUG,
        )
    }
    single(ConnectionsQualifier.DaoFactory) { get<ConcurrentFactory<AppDatabase>>().asDaoFactory { it.pairedDeviceDao() } }
    single(DestinationsQualifier.DaoFactory) { get<ConcurrentFactory<AppDatabase>>().asDaoFactory { it.destinationDao() } }
    single(LocaleQualifier.DaoFactory) { get<ConcurrentFactory<AppDatabase>>().asDaoFactory { it.localeDao() } }
    single(ProfileQualifier.DaoFactory) { get<ConcurrentFactory<AppDatabase>>().asDaoFactory { it.profileDao() } }
    single(SessionQualifier.DaoFactory) { get<ConcurrentFactory<AppDatabase>>().asDaoFactory { it.sessionDao() } }
}

private fun <T : Any> ConcurrentFactory<AppDatabase>.asDaoFactory(
    extract: (AppDatabase) -> T,
): ConcurrentFactory<T> = object : ConcurrentFactory<T>() {
    override suspend fun create(): T = extract(this@asDaoFactory.get())
}

internal val appModule = module {
    // alphabetically sorted
    includes(
        altitudeModule,
        bleModule,
        concurrentModule,
        connectionSessionBridgeImplModule,
        connectionsModule,
        databaseModule,
        destinationNutritionBridgeImplModule,
        destinationPoiBridgeImplModule,
        destinationSessionBridgeImplModule,
        destinationsModule,
        distanceModule,
        errorMapperModule,
        idModule,
        localeModule,
        locationModule,
        mainModule,
        mapModule,
        nutritionModule,
        nutritionSessionBridgeImplModule,
        nutritionSettingsBridgeImplModule,
        poiModule,
        poiSettingsBridgeImplModule,
        powerSensorModule,
        profileModule,
        profileSessionBridgeImplModule,
        sessionModule,
        sessionSettingsBridgeImplModule,
        settingsModule,
        themeModule,
    )
}
