package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DubbingDao {
    @Query("SELECT * FROM dubbing_projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<DubbingProject>>

    @Query("SELECT * FROM dubbing_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): DubbingProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: DubbingProject): Long

    @Update
    suspend fun updateProject(project: DubbingProject)

    @Delete
    suspend fun deleteProject(project: DubbingProject)

    @Query("DELETE FROM dubbing_projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
