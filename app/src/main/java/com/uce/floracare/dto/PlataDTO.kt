package com.uce.floracare.dto

data class Planta(

    val id: Int,

    val nombre_comun: String,
    val nombre_cientifico: String,
    val imagen: String,
    val tipo: String,
    val descripcion: String,
    val ciclo_vida: String,
    val nivel_cuidado: String,
    val caracteristicas: Caracteristicas,
    val riego: Riego,
    val luz_solar: Array<String>,
    val temperatura: TemperaturaDTO,
    val esDestacada : Boolean
)


