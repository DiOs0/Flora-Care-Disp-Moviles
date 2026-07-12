package com.uce.floracare.utils

object PlantConstants {
    val wateringOptions = mapOf(
        "Cada día" to 1,
        "Cada 2 días" to 2,
        "Cada 3 días" to 3,
        "Cada 5 días" to 5,
        "Cada semana" to 7,
        "Cada 10 días" to 10,
        "Cada 2 semanas" to 14,
        "Cada mes" to 30
    )
    
    val plantTypes = arrayOf("Suculenta", "Cactus", "Follaje", "Flor", "Arbusto", "Árbol")
    val growthCycles = arrayOf("Perenne", "Anual", "Bienal")
    val careLevels = arrayOf("Bajo", "Medio", "Alto")
}
