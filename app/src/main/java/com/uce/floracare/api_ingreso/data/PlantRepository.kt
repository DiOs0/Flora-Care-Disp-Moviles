package com.uce.floracare.api_ingreso.data

import android.net.Uri
import android.util.Log

class PlantRepository(
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager
) {

    /**
     * Guarda una nueva planta realizando el guardado local de la imagen (workaround para Spark plan)
     * y el guardado de datos en Firestore.
     */
    suspend fun saveNewPlant(
        name: String,
        species: String,
        location: String,
        lastWatered: String,
        photoUri: Uri
    ): Result<Unit> {
        return try {
            // 1. Guardar imagen LOCALMENTE (ya que Storage está bloqueado por el plan Spark)
            val saveLocalResult = storageManager.saveImageLocally(photoUri)
            
            // Si falla el guardado local, usamos una cadena vacía o una imagen por defecto
            val imageUrl = saveLocalResult.getOrDefault("")

            // 2. Obtener IDs existentes para generar el siguiente ID secuencial
            val existingIds = firestoreManager.getExistingIds()
            val newId = (existingIds.maxOrNull() ?: 0) + 1

            // 3. Crear instancia de PlantEntity
            val plant = PlantEntity(
                id = newId,
                nombreComun = name,
                nombreCientifico = species,
                imagen = imageUrl, // Aquí guardamos la ruta local (file://...)
                tipo = location,
                descripcion = "Planta guardada localmente (Cloud Storage deshabilitado)",
                caracteristicas = Caracteristicas(
                    indoor = location == "Interior"
                ),
                riego = Riego(
                    frecuencia = "Último riego registrado: $lastWatered"
                )
            )

            // 4. Guardar en Firestore en una colección SEPARADA
            firestoreManager.uploadUserPlant(plant)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error en saveNewPlant", e)
            Result.failure(e)
        }
    }
}
