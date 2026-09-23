package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRecordingDao {
    @Query("SELECT * FROM voice_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<VoiceRecordingEntity>>

    @Query("SELECT * FROM voice_recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): VoiceRecordingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: VoiceRecordingEntity): Long

    @Update
    suspend fun updateRecording(recording: VoiceRecordingEntity)

    @Delete
    suspend fun deleteRecording(recording: VoiceRecordingEntity)

    @Query("DELETE FROM voice_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
