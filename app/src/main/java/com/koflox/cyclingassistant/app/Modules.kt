package com.koflox.cyclingassistant.app

import android.content.Context
import com.koflox.concurrent.ConcurrentFactory
import com.koflox.connections.data.source.local.dao.PairedDeviceDao
import com.koflox.cyclingassistant.BuildConfig
import com.koflox.cyclingassistant.data.AppDatabase
import com.koflox.cyclingassistant.data.DatabasePassphraseManager
import com.koflox.cyclingassistant.data.DatabasePassphraseManagerImpl
import com.koflox.cyclingassistant.data.RoomDatabaseFactory
import com.koflox.cyclingassistant.locale.LocalizedContextProviderImpl
import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.di.ConnectionsDaoFactory
import com.koflox.di.DefaultDispatcher
import com.koflox.di.DestinationsDaoFactory
import com.koflox.di.IoDispatcher
import com.koflox.di.LocaleDaoFactory
import com.koflox.di.ProfileDaoFactory
import com.koflox.di.SessionDaoFactory
import com.koflox.locale.data.source.local.dao.LocaleDao
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.profile.data.source.local.dao.ProfileDao
import com.koflox.session.data.source.local.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseHiltModule {

    @Provides
    @Singleton
    fun provideDatabasePassphraseManager(
        @ApplicationContext context: Context,
    ): DatabasePassphraseManager = DatabasePassphraseManagerImpl(context = context)

    @Provides
    @Singleton
    fun provideDatabaseFactory(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager,
        @IoDispatcher dispatcherIo: CoroutineDispatcher,
    ): ConcurrentFactory<AppDatabase> = RoomDatabaseFactory(
        context = context,
        passphraseManager = passphraseManager,
        dispatcherIo = dispatcherIo,
        isEncryptionEnabled = !BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    @ConnectionsDaoFactory
    fun provideConnectionsDaoFactory(
        dbFactory: ConcurrentFactory<AppDatabase>,
    ): ConcurrentFactory<PairedDeviceDao> = dbFactory.asDaoFactory { it.pairedDeviceDao() }

    @Provides
    @Singleton
    @DestinationsDaoFactory
    fun provideDestinationsDaoFactory(
        dbFactory: ConcurrentFactory<AppDatabase>,
    ): ConcurrentFactory<DestinationDao> = dbFactory.asDaoFactory { it.destinationDao() }

    @Provides
    @Singleton
    @LocaleDaoFactory
    fun provideLocaleDaoFactory(
        dbFactory: ConcurrentFactory<AppDatabase>,
    ): ConcurrentFactory<LocaleDao> = dbFactory.asDaoFactory { it.localeDao() }

    @Provides
    @Singleton
    @ProfileDaoFactory
    fun provideProfileDaoFactory(
        dbFactory: ConcurrentFactory<AppDatabase>,
    ): ConcurrentFactory<ProfileDao> = dbFactory.asDaoFactory { it.profileDao() }

    @Provides
    @Singleton
    @SessionDaoFactory
    fun provideSessionDaoFactory(
        dbFactory: ConcurrentFactory<AppDatabase>,
    ): ConcurrentFactory<SessionDao> = dbFactory.asDaoFactory { it.sessionDao() }
}

private fun <T : Any> ConcurrentFactory<AppDatabase>.asDaoFactory(
    extract: (AppDatabase) -> T,
): ConcurrentFactory<T> = object : ConcurrentFactory<T>() {
    override suspend fun create(): T = extract(this@asDaoFactory.get())
}

@Module
@InstallIn(SingletonComponent::class)
internal object AppHiltModule {

    @Provides
    @Singleton
    fun provideLocalizedContextProvider(
        @ApplicationContext applicationContext: Context,
        observeLocaleUseCase: ObserveLocaleUseCase,
        @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
    ): LocalizedContextProvider = LocalizedContextProviderImpl(
        applicationContext = applicationContext,
        observeLocaleUseCase = observeLocaleUseCase,
        dispatcherDefault = dispatcherDefault,
    )
}
