package com.uce.floracare.activities.Reyes_MiJardin

import com.uce.floracare.data.remote.dto.PlantEntity
import androidx.annotation.DrawableRes
import java.io.Serializable

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

    val imageUrl: String

): Serializable


fun PlantEntity.toPlant(): Plant {

    return Plant(
        id=id,
        nombre=nombreComun,
        nombreCientifico=nombreCientifico,
        tipo=tipo,
        luz=luzSolar.joinToString(", "),
        riego=riego.frecuencia,
        imagenRes=android.R.drawable.sym_def_app_icon,
        esDestacada=false,
        necesitaAgua=riego.frecuencia.equals(
            "Frequent",
            true
        ),
        imageUrl=imagen
    )

}