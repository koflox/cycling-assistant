package com.koflox.destinations.data.source.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.destinations.data.source.local.entity.DestinationLocal

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destinations")
    suspend fun getAllDestinations(): List<DestinationLocal>

    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: String): DestinationLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(destinations: List<DestinationLocal>)
}
