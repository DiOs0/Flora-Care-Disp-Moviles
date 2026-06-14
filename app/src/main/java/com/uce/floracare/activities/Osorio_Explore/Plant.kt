package com.uce.floracare.activities.Osorio_Explore

import com.uce.floracare.api_ingreso.data.PlantEntity

data class Plant(


    val id: Int,

    val nombre: String,
    val nombreCientifico: String,
    val indoor: Boolean,
    val nivelCuidado: String,
    val imagenUrl: String,
)

fun PlantEntity.toExplorePlant(): Plant {
    return Plant(
        id = id,
        nombre = nombreComun,
        nombreCientifico = nombreCientifico,
        indoor = caracteristicas.indoor,
        nivelCuidado = nivelCuidado,
        imagenUrl = imagen
    )
}
