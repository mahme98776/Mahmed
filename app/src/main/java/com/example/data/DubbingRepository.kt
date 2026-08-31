package com.example.data

import kotlinx.coroutines.flow.Flow

class DubbingRepository(private val dao: DubbingDao) {
    val allProjects: Flow<List<DubbingProject>> = dao.getAllProjects()

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
}
