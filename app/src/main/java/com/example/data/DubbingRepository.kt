package com.example.data

import kotlinx.coroutines.flow.Flow

class DubbingRepository(
    private val dao: DubbingDao,
    private val recordingDao: VoiceRecordingDao
) {
    val allProjects: Flow<List<DubbingProject>> = dao.getAllProjects()
    val allRecordings: Flow<List<VoiceRecordingEntity>> = recordingDao.getAllRecordings()

    suspend fun getProjectById(id: Long): DubbingProject? = dao.getProjectById(id)

    suspend fun saveProject(project: DubbingProject): Long {
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: DubbingProject) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: DubbingProject) {
        dao.deleteProject(project)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    // Voice Recordings Operations
    suspend fun saveVoiceRecording(recording: VoiceRecordingEntity): Long {
        return recordingDao.insertRecording(recording)
    }

    suspend fun deleteVoiceRecording(recording: VoiceRecordingEntity) {
        recordingDao.deleteRecording(recording)
    }

    suspend fun deleteVoiceRecordingById(id: Long) {
        recordingDao.deleteById(id)
    }
}
