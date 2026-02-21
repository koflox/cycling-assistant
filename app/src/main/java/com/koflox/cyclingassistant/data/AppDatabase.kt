package com.koflox.cyclingassistant.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.locale.data.source.local.dao.LocaleDao
import com.koflox.locale.data.source.local.entity.LocaleSettingsEntity
import com.koflox.profile.data.source.local.dao.ProfileDao
import com.koflox.profile.data.source.local.entity.ProfileSettingsEntity
import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity

// TODO: find a way to put in a separate module, e.g. :feature:database
@Database(
    entities = [
        DestinationLocal::class,
        LocaleSettingsEntity::class,
        ProfileSettingsEntity::class,
        SessionEntity::class,
        TrackPointEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "cycling_assistant_db"
    }

    abstract fun destinationDao(): DestinationDao

    abstract fun localeDao(): LocaleDao

    abstract fun profileDao(): ProfileDao

    abstract fun sessionDao(): SessionDao
}
