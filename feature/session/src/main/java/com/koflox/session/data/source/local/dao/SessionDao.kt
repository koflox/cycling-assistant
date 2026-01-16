package com.koflox.session.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.koflox.session.data.source.local.entity.SessionEntity
import com.koflox.session.data.source.local.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

// TODO: cleanup unused generated methods
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

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = 'COMPLETED' ORDER BY startTimeMs DESC")
    fun observeCompletedSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTimeMs DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoint(trackPoint: TrackPointEntity)

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getTrackPoints(sessionId: String): List<TrackPointEntity>

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
