package com.uce.floracare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uce.floracare.data.remote.dto.Caracteristicas
import com.uce.floracare.data.remote.dto.PlantEntity as RemotePlantEntity

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey 
    val id: String, // ID que viene de Firestore
    val nombreComun: String,
    val nombreCientifico: String,
    val imagen: String, // URL de Cloudinary
    val nivelCuidado: String,
    val esInterior: Boolean,
    val userId: String, // Identificador: "catalog" o el UID del usuario de Firebase
    val wateringFrequencyDays: Int = 7, // Frecuencia por defecto
    val lastWateredDate: Long = System.currentTimeMillis() // Última vez que se regó
)

fun PlantEntity.toRemoteEntity(): RemotePlantEntity {
    return RemotePlantEntity(
        firestoreId = if (userId != "catalog") id else "",
        nombreComun = nombreComun,
        nombreCientifico = nombreCientifico,
        imagen = imagen,
        nivelCuidado = nivelCuidado,
        caracteristicas = Caracteristicas(indoor = esInterior),
        ultimoRiego = lastWateredDate,
        wateringFrequencyDays = wateringFrequencyDays
    )
}
