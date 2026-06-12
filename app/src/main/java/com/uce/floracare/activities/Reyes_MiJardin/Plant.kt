package com.uce.floracare.activities.Reyes_MiJardin

import androidx.annotation.DrawableRes

data class Plant(
    val id: Int,
    val nombre: String,
    val nombreCientifico: String,
    val tipo: String,
    val luz: String,
    val riego: String,
    @DrawableRes val imagenRes: Int,
    val esDestacada: Boolean,

    //Prueba para MiJardin
    val necesitaAgua: Boolean,
    val imageUrl: Int // Representa la referencia R.string que almacena la URL de la planta
)