package com.uce.floracare.domain.model

/**
 * Representa los posibles estados de riego de una planta.
 */
enum class WateringStatus {
    NORMAL,             // Todo bien
    ATENCION_REQUERIDA, // Se acerca o es el día de riego
    URGENTE             // Han pasado más de 1 o 2 días del riego programado
}
