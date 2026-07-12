package com.uce.floracare.domain.usecase

import com.uce.floracare.domain.model.EstadisticasJardin
import com.uce.floracare.repositories.PlantRepository
import com.uce.floracare.repositories.TaskRepository
import com.uce.floracare.repositories.connections.remote.firebase.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlin.math.max

/**
 * Caso de Uso para calcular las estadísticas globales del jardín de forma reactiva.
 * Combina los flujos de plantas y tareas para generar un resumen del estado del jardín.
 */
class ObtenerEstadisticasJardinUseCase(
    private val plantRepository: PlantRepository,
    private val taskRepository: TaskRepository,
    private val authManager: AuthManager
) {
    operator fun invoke(): Flow<EstadisticasJardin> {
        val userId = authManager.getCurrentUserId() ?: return flowOf(EstadisticasJardin())

        // Nota: Se asume que TaskRepository implementará getTasksStream en el siguiente paso
        // para cumplir con el requisito de reactividad SSOT.
        return combine(
            plantRepository.getGardenPlantsStream(userId),
            taskRepository.getTasksStream(userId)
        ) { plants, tasks ->
            val totalPlantas = plants.size
            val pendingTasks = tasks.count { !it.completed }
            
            // Lógica de salud: Basada en tareas pendientes vs total de plantas
            // Penalizamos un 10% por cada tarea pendiente sobre el total posible.
            // Si no hay plantas, la salud se considera óptima (100).
            val saludBase = 100
            val penalizacion = if (totalPlantas > 0) (pendingTasks * 10) else 0
            val saludGeneral = max(0, saludBase - penalizacion)

            EstadisticasJardin(
                totalPlantas = totalPlantas,
                tareasPendientes = pendingTasks,
                saludGeneral = saludGeneral
            )
        }
    }
}
