package com.uce.floracare.api_ingreso.data

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL

class StorageManager {

    private val storage = FirebaseStorage.getInstance()
    private val plantsImagesRef = storage.reference.child("plants_images")

    suspend fun uploadPlantImage(plantId: Int, perenualUrl: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bytes = URL(perenualUrl).openStream().readBytes()

                val imageRef = plantsImagesRef.child("plant_$plantId.jpg")
                imageRef.putBytes(bytes).await()

                val downloadUrl = imageRef.downloadUrl.await()

                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
