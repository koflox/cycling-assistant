package com.koflox.locale.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koflox.locale.data.source.local.entity.LocaleSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocaleDao {

    @Query("SELECT * FROM locale_settings WHERE id = 0")
    fun observeSettings(): Flow<LocaleSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: LocaleSettingsEntity)
}
