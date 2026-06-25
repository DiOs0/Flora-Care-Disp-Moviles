package com.uce.floracare.data.local.dto

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val fotoPerfil: String,
    val preferencias: Preferencias
)

data class Preferencias(
    val recordatoriosRiego: Boolean,
    val notificacionesCatalogo: Boolean
)