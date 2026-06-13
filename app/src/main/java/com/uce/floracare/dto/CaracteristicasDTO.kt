package com.uce.floracare.dto

data class Caracteristicas(
    val medicinal: Boolean,
    val indoor: Boolean,
    val tropical: Boolean,
    val resistente_sequia: Boolean,
    val toxica_humanos: Boolean,
    val toxica_mascotas: Boolean
)