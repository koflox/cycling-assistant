package com.koflox.cyclingassistant.app

import androidx.room.Room
import com.koflox.concurrent.concurrentModule
import com.koflox.cyclingassistant.data.AppDatabase
import com.koflox.destinations.di.destinationsModule
import com.koflox.destinationsession.bridge.impl.di.bridgeImplModule
import com.koflox.distance.di.distanceModule
import com.koflox.id.di.idModule
import com.koflox.location.locationModule
import com.koflox.session.di.sessionModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }

    single { get<AppDatabase>().destinationDao() }

    single { get<AppDatabase>().sessionDao() }
}

internal val appModule = module {
    includes(
        concurrentModule,
        distanceModule,
        idModule,
        locationModule,
        databaseModule,
        sessionModule,
        bridgeImplModule,
        destinationsModule,
    )
}
