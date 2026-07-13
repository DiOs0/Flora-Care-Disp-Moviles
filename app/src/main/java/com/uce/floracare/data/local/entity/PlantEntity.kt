package com.uce.floracare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.Riego
import com.uce.floracare.data.remote.dto.Temperatura
import kotlin.String
import com.uce.floracare.data.remote.dto.PlantEntity as RemotePlantEntity

@Entity(tableName = "plants")
data class PlantEntity(

    @PrimaryKey
    val id: String,

    val nombreComun: String,
    val nombreCientifico: String,
    val imagen: String,

    val tipo: String,
    val descripcion: String,
    val cicloVida: String,
    val nivelCuidado: String,

    val medicinal: Boolean,
    val esInterior: Boolean,
    val tropical: Boolean,
    val resistenteSequia: Boolean,
    val toxicaHumanos: Boolean,
    val toxicaMascotas: Boolean,

    val frecuenciaRiego: String,
    val cadaValorRiego: String,

    val luzSolar: String,

    val temperaturaMin: Int,
    val temperaturaMax: Int,
    val temperaturaDescripcion: String,

    val userId: String,

    val wateringFrequencyDays: Int = 7,

    val lastWateredDate: Long =
        System.currentTimeMillis(),

    val nextWateringTimestamp: Long =
        System.currentTimeMillis() +
                (7L * 24L * 60L * 60L * 1000L)


//                wateringFrequencyDays = 1
//ultimoRiego = una fecha anterior
//nextWateringTimestamp = una fecha anterior a la actual
)

fun PlantEntity.toRemoteEntity(): RemotePlantEntity {

    return RemotePlantEntity(

        firestoreId =
            if (userId != "catalog") {
                id
            } else {
                ""
            },

        nombreComun = nombreComun,

        nombreCientifico = nombreCientifico,

        imagen = imagen,

        tipo = tipo,

        descripcion = descripcion,

        cicloVida = cicloVida,

        nivelCuidado = nivelCuidado,

        caracteristicas = Caracteristicas(
            medicinal = medicinal,
            indoor = esInterior,
            tropical = tropical,
            resistenteSequia = resistenteSequia,
            toxicaHumanos = toxicaHumanos,
            toxicaMascotas = toxicaMascotas
        ),

        riego = Riego(
            frecuencia = frecuenciaRiego,
            cadaValor = cadaValorRiego
        ),

        luzSolar =
            if (luzSolar.isBlank()) {
                emptyList()
            } else {
                luzSolar.split("|")
            },

        temperatura = Temperatura(
            min = temperaturaMin,
            max = temperaturaMax,
            descripcion = temperaturaDescripcion
        ),

        ultimoRiego = lastWateredDate,

        nextWateringTimestamp =
            nextWateringTimestamp,

        wateringFrequencyDays =
            wateringFrequencyDays
    )
}


