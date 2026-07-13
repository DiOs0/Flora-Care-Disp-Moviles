package com.uce.floracare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uce.floracare.domain.model.TaskEntity as DomainTaskEntity

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey
    val firestoreId: String = "",

    val plantFirestoreId: String = "",

    val plantName: String = "",

    val title: String = "",

    val description: String = "",

    val createdAt: Long = 0L,

    val completed: Boolean = false,

    val userId: String = ""
)

fun TaskEntity.toDomain(): DomainTaskEntity {

    return DomainTaskEntity(
        firestoreId = firestoreId,
        plantFirestoreId = plantFirestoreId,
        plantName = plantName,
        title = title,
        description = description,
        createdAt = createdAt,
        completed = completed
    )
}

fun DomainTaskEntity.toLocal(
    userId: String
): TaskEntity {

    return TaskEntity(
        firestoreId = firestoreId,
        plantFirestoreId = plantFirestoreId,
        plantName = plantName,
        title = title,
        description = description,
        createdAt = createdAt,
        completed = completed,
        userId = userId
    )
}