package com.uce.floracare.repositories

import com.uce.floracare.data.local.dao.TaskDao
import com.uce.floracare.data.local.entity.toDomain
import com.uce.floracare.data.local.entity.toLocal
import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TaskRepository(
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager,
    private val taskDao: TaskDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    /**
     * SSOT: La UI observa este Flow reactivo desde Room.
     */
    fun getTasksStream(userId: String): Flow<List<TaskEntity>> {
        // Disparamos la actualización en segundo plano
        repositoryScope.launch {
            refreshTasks()
        }
        return taskDao.getTasksByUserId(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Sincroniza las tareas desde Firebase a la base de datos local.
     */
    suspend fun refreshTasks() {
        val userId = authManager.getCurrentUserId() ?: return
        val remoteResult = firestoreManager.getTasks()
        remoteResult.onSuccess { remoteTasks ->
            val localEntities = remoteTasks.map { it.toLocal(userId) }
            taskDao.deleteTasksByUserId(userId)
            taskDao.insertTasks(localEntities)
        }
    }

    suspend fun getPendingTasks(): Result<List<TaskEntity>> {
        return firestoreManager.getTasks()
    }

    suspend fun saveTask(task: TaskEntity): Result<Unit> {
        val result = firestoreManager.saveTask(task)
        if (result.isSuccess) refreshTasks()
        return result
    }

    suspend fun updateTask(task: TaskEntity): Result<Unit> {
        val result = firestoreManager.updateTask(task)
        if (result.isSuccess) refreshTasks()
        return result
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        val result = firestoreManager.deleteTask(taskId)
        if (result.isSuccess) refreshTasks()
        return result
    }

    suspend fun getTasks(): Result<List<TaskEntity>> {
        return firestoreManager.getTasks()
    }
}
