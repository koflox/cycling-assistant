package com.koflox.strava.impl.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.strava.impl.data.source.local.entity.StravaSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StravaSyncDao {

    @Query("SELECT * FROM strava_sync_status WHERE sessionId = :sessionId")
    fun observe(sessionId: String): Flow<StravaSyncEntity?>

    @Query("SELECT * FROM strava_sync_status WHERE sessionId = :sessionId")
    suspend fun get(sessionId: String): StravaSyncEntity?

    @Query("SELECT * FROM strava_sync_status")
    fun observeAll(): Flow<List<StravaSyncEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StravaSyncEntity)

    @Query("DELETE FROM strava_sync_status WHERE sessionId = :sessionId")
    suspend fun delete(sessionId: String)

    @Query("DELETE FROM strava_sync_status")
    suspend fun deleteAll()
}
