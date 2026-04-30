package com.koflox.strava.impl.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StravaTokenDao {

    @Query("SELECT * FROM strava_tokens WHERE id = 0")
    suspend fun get(): StravaTokenEntity?

    @Query("SELECT * FROM strava_tokens WHERE id = 0")
    fun observe(): Flow<StravaTokenEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StravaTokenEntity)

    @Query("DELETE FROM strava_tokens")
    suspend fun delete()
}
