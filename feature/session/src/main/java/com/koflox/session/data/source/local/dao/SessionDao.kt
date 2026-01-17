package com.koflox.session.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.SessionWithTrackPoints
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

@Suppress("ComplexInterface")
@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoints(trackPoints: List<TrackPointEntity>)

    @Transaction
    suspend fun insertSessionWithTrackPoints(session: SessionEntity, trackPoints: List<TrackPointEntity>) {
        insertSession(session)
        if (trackPoints.isNotEmpty()) {
            insertTrackPoints(trackPoints)
        }
    }

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionWithTrackPoints(sessionId: String): SessionWithTrackPoints?

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Transaction
    @Query("SELECT * FROM sessions WHERE status IN (:statuses) ORDER BY startTimeMs DESC")
    fun observeSessionsByStatuses(statuses: List<String>): Flow<List<SessionWithTrackPoints>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE status IN (:statuses) ORDER BY startTimeMs DESC LIMIT 1")
    fun observeFirstSessionByStatuses(statuses: List<String>): Flow<SessionWithTrackPoints?>
}
