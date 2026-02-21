package com.koflox.profile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.profile.data.source.local.entity.ProfileSettingsEntity

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile_settings WHERE id = 0")
    suspend fun getSettings(): ProfileSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ProfileSettingsEntity)
}
