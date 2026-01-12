package com.koflox.cyclingassistant.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koflox.cyclingassistant.data.source.local.database.dao.DestinationDao
import com.koflox.cyclingassistant.data.source.local.entity.DestinationLocal

@Database(
    entities = [DestinationLocal::class],
    version = 1,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "cycling_assistant_db"
    }

    abstract fun destinationDao(): DestinationDao

}
