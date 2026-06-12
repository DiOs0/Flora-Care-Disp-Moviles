package com.uce.floracare.activities.Reyes_MiJardin

import com.uce.floracare.api_ingreso.data.PlantEntity
import androidx.annotation.DrawableRes

data class Plant(
    val id: Int,
    val nombre: String,
    val nombreCientifico: String,
    val tipo: String,
    val luz: String,
    val riego: String,

    @DrawableRes
    val imagenRes: Int,

    val esDestacada: Boolean,
    val necesitaAgua: Boolean,

    // URL real de la imagen
    val imageUrl: String
)

fun PlantEntity.toPlant(): Plant {

    return Plant(
        id = id,
        nombre = nombreComun,
        nombreCientifico = nombreCientifico,
        tipo = tipo,
        luz = luzSolar.joinToString(", "),
        riego = riego.frecuencia,
        imagenRes = android.R.drawable.sym_def_app_icon,
        esDestacada = false,

        // Ejemplo temporal
        necesitaAgua = riego.frecuencia.equals("Frequent", true),

        imageUrl = imagen
    )
}