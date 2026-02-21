package com.koflox.cyclingassistant.data

import android.content.Context
import androidx.room.Room
import com.koflox.concurrent.ConcurrentFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

internal class RoomDatabaseFactory(
    private val context: Context,
    private val passphraseManager: DatabasePassphraseManager,
    private val dispatcherIo: CoroutineDispatcher,
    private val isEncryptionEnabled: Boolean,
) : ConcurrentFactory<AppDatabase>() {

    override suspend fun create(): AppDatabase = withContext(dispatcherIo) {
        if (isEncryptionEnabled) System.loadLibrary("sqlcipher")
        buildDatabase().recoverIfCorrupted()
    }

    private fun buildDatabase(): AppDatabase {
        val builder = Room.databaseBuilder(
            context, AppDatabase::class.java, AppDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration(true)
        if (isEncryptionEnabled) {
            val passphrase = passphraseManager.getPassphrase()
            builder.openHelperFactory(SupportOpenHelperFactory(passphrase))
        }
        return builder.build()
    }

    // If Keystore key is lost, DatabasePassphraseManager regenerates a new passphrase.
    // The old DB file is encrypted with the old passphrase and can't be opened.
    // fallbackToDestructiveMigration does NOT help — SQLCipher fails at open time,
    // before Room checks schema version. So we force-open, catch the failure,
    // delete the DB file, and rebuild fresh.
    // TODO: notify the user about the data reset
    @Suppress("TooGenericExceptionCaught")
    private fun AppDatabase.recoverIfCorrupted(): AppDatabase = try {
        openHelper.writableDatabase
        this
    } catch (_: Exception) {
        close()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        buildDatabase()
    }
}
