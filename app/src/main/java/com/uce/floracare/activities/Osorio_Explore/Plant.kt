package com.uce.floracare.activities.Osorio_Explore

import androidx.annotation.DrawableRes

data class Plant(
    val id: Int,
    val nombre: String,
    val nombreCientifico: String,
    val tipo: String,
    val luz: String,
    val riego: String,
    @DrawableRes val imagenRes: Int,
    val esDestacada: Boolean
)