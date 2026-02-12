package com.koflox.cyclingassistant.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koflox.destinations.data.source.local.database.dao.DestinationDao
import com.koflox.destinations.data.source.local.entity.DestinationLocal
import com.koflox.session.data.source.local.dao.SessionDao
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity

// TODO: find a way to put in a separate module, e.g. :feature:database
@Database(
    entities = [
        DestinationLocal::class,
        SessionEntity::class,
        TrackPointEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun destinationDao(): DestinationDao

    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "cycling_assistant_db"
    }
}
