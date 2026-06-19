package com.uce.floracare.domain.model

enum class TaskType {
    INFO,
    WATERING,
    MISTING,
    CARE
}

data class PlantTask(
    val title: String,
    val description: String,
    val taskType: TaskType
)
