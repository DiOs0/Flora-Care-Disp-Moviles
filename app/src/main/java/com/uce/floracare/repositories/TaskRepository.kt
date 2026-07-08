package com.uce.floracare.repositories

import com.uce.floracare.domain.model.TaskEntity
import com.uce.floracare.repositories.connections.remote.firebase.FirestoreManager

class TaskRepository(
    private val firestoreManager: FirestoreManager
) {

    suspend fun getPendingTasks(): Result<List<TaskEntity>> {
        return firestoreManager.getTasks()
    }

    suspend fun saveTask(
        task: TaskEntity
    ): Result<Unit> {

        return firestoreManager.saveTask(task)

    }

    suspend fun updateTask(
        task: TaskEntity
    ): Result<Unit> {

        return firestoreManager.updateTask(task)

    }

    suspend fun deleteTask(
        taskId: String
    ): Result<Unit> {

        return firestoreManager.deleteTask(taskId)

    }

    suspend fun getTasks(): Result<List<TaskEntity>> {

        return firestoreManager.getTasks()

    }



}