package com.uce.floracare.dto

data class PlantDto(
    val id: Int,
    val common_name: String?,
    val scientific_name: List<String>?,
    val default_image: ImageDto?
)