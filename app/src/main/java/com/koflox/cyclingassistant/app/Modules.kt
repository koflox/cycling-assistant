package com.koflox.cyclingassistant.app

import androidx.room.Room
import com.koflox.altitude.di.altitudeModule
import com.koflox.concurrent.concurrentModule
import com.koflox.cyclingassistant.MainViewModel
import com.koflox.cyclingassistant.data.AppDatabase
import com.koflox.cyclingassistant.locale.LocalizedContextProviderImpl
import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.destinationnutrition.bridge.impl.di.destinationNutritionBridgeImplModule
import com.koflox.destinations.di.destinationsModule
import com.koflox.destinationsession.bridge.impl.di.destinationSessionBridgeImplModule
import com.koflox.distance.di.distanceModule
import com.koflox.error.di.errorMapperModule
import com.koflox.id.di.idModule
import com.koflox.locale.di.localeModule
import com.koflox.location.locationModule
import com.koflox.nutrition.di.nutritionModule
import com.koflox.nutritionsession.bridge.impl.di.nutritionSessionBridgeImplModule
import com.koflox.nutritionsettings.bridge.impl.di.nutritionSettingsBridgeImplModule
import com.koflox.profile.di.profileModule
import com.koflox.profilesession.bridge.impl.di.profileSessionBridgeImplModule
import com.koflox.session.di.sessionModule
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
        )
    }
    viewModel {
        MainViewModel(
            observeThemeUseCase = get(),
            observeLocaleUseCase = get(),
        )
    }
}

private val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration(true).build()
    }

    single { get<AppDatabase>().destinationDao() }

    single { get<AppDatabase>().sessionDao() }
}

internal val appModule = module {
    // alphabetically sorted
    includes(
        altitudeModule,
        concurrentModule,
        databaseModule,
        destinationNutritionBridgeImplModule,
        destinationSessionBridgeImplModule,
        destinationsModule,
        distanceModule,
        errorMapperModule,
        idModule,
        localeModule,
        locationModule,
        mainModule,
        nutritionModule,
        nutritionSessionBridgeImplModule,
        nutritionSettingsBridgeImplModule,
        profileModule,
        profileSessionBridgeImplModule,
        sessionModule,
        settingsModule,
        themeModule,
    )
}
