package com.uce.floracare.activities.Milan_Ajustes.dto

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