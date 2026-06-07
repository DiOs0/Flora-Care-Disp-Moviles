package com.uce.floracare.activities.Reyes_MiJardin

import android.R
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
    val necesitaAgua : Boolean,
    val imageUrl : String
)