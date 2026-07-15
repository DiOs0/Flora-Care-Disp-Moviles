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

    private val repositoryScope =
        CoroutineScope(Dispatchers.IO)

    fun getTasksStream(
        userId: String
    ): Flow<List<TaskEntity>> {

        repositoryScope.launch {
            refreshTasks()
        }

        return taskDao
            .getTasksByUserId(userId)
            .map { localTasks ->

                localTasks.map {
                    it.toDomain()
                }
            }
    }

    suspend fun refreshTasks() {

        val userId =
            authManager.getCurrentUserId()
                ?: return

        val result =
            firestoreManager.getTasks()

        result.onSuccess { remoteTasks ->

            val validTasks =
                remoteTasks.filter {
                    it.firestoreId.isNotBlank()
                }

            val localTasks =
                validTasks.map {
                    it.toLocal(userId)
                }

            taskDao.deleteTasksByUserId(
                userId
            )

            if (localTasks.isNotEmpty()) {

                taskDao.insertTasks(
                    localTasks
                )
            }
        }
    }

    suspend fun getPendingTasks():
            Result<List<TaskEntity>> {

        return firestoreManager.getTasks()
    }

    suspend fun getPendingTasksFromLocal(): Result<List<TaskEntity>> {
        return try {
            val userId = authManager.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            val localTasks = taskDao.getPendingTasksList(userId)
            Result.success(localTasks.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTask(
        task: TaskEntity
    ): Result<Unit> {

        val result =
            firestoreManager.saveTask(task)

        if (result.isSuccess) {
            refreshTasks()
        }

        return result
    }

    suspend fun updateTask(
        task: TaskEntity
    ): Result<Unit> {

        val result =
            firestoreManager.updateTask(task)

        if (result.isSuccess) {
            refreshTasks()
        }

        return result
    }

    suspend fun deleteTask(
        taskId: String
    ): Result<Unit> {

        if (taskId.isBlank()) {

            return Result.failure(
                Exception(
                    "El identificador de la tarea está vacío"
                )
            )
        }

        val result =
            firestoreManager.deleteTask(
                taskId
            )

        if (result.isSuccess) {
            refreshTasks()
        }

        return result
    }

    suspend fun deleteTasksByPlantId(
        plantFirestoreId: String
    ): Result<Unit> {

        return try {

            val tasks =
                getPendingTasks()
                    .getOrThrow()

            tasks
                .filter {
                    it.plantFirestoreId ==
                            plantFirestoreId
                }
                .forEach {

                    deleteTask(
                        it.firestoreId
                    ).getOrThrow()
                }

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}