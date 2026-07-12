package com.uce.floracare.domain.model

/**
 * Modelo de dominio para representar las estadísticas del jardín del usuario.
 * @param totalPlantas Número total de plantas en el jardín.
 * @param tareasPendientes Número de tareas que aún no han sido completadas.
 * @param saludGeneral Porcentaje (0-100) que representa el estado general de cuidado.
 */
data class EstadisticasJardin(
    val totalPlantas: Int = 0,
    val tareasPendientes: Int = 0,
    val saludGeneral: Int = 0
)
