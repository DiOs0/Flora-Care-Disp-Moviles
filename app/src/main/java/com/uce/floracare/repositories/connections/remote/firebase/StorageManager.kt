package com.uce.floracare.repositories.connections.remote.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class StorageManager(private val context: Context) {

    // Usamos el singleton predeterminado que lee del google-services.json
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val plantsImagesRef = storageRef.child("plants_images")

    init {
        Log.d("StorageManager", "Bucket en uso: ${storage.reference.bucket}")
        Log.d("StorageManager", "Ruta de referencia base: ${plantsImagesRef.path}")
    }

    suspend fun uploadPlantImage(plantId: Int, perenualUrl: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("StorageManager", "Iniciando descarga de imagen: $perenualUrl")
                val bytes = URL(perenualUrl).openStream().readBytes()

                if (bytes.isEmpty()) {
                    return@withContext Result.failure(Exception("La imagen descargada está vacía"))
                }

                val imageRef = plantsImagesRef.child("plant_$plantId.jpg")
                Log.d("StorageManager", "Subiendo a: ${imageRef.path}")

                checkAuth()

                imageRef.putBytes(bytes).await()
                val downloadUrl = imageRef.downloadUrl.await()

                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Log.e("StorageManager", "Error subiendo imagen de planta (perenual)", e)
                if (e is StorageException) {
                    Log.e("StorageManager", "Storage Error Code: ${e.errorCode}")
                }
                Result.failure(e)
            }
        }

    suspend fun uploadUserPlantImage(uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Verificar si podemos abrir el stream antes de intentar la subida
                context.contentResolver.openInputStream(uri)?.use {
                    Log.d("StorageManager", "Stream de archivo abierto correctamente para $uri")
                } ?: throw Exception("No se pudo abrir el stream para el URI: $uri")

                val timeStamp = System.currentTimeMillis()
                val imageRef = plantsImagesRef.child("user_plant_$timeStamp.jpg")

                Log.d("StorageManager", "Subiendo archivo local $uri a ${imageRef.path}")

                checkAuth()

                // Intentamos la subida
                val uploadTask = imageRef.putFile(uri)

                // Agregamos listeners para depuración si es necesario, pero await() es suficiente para el resultado
                uploadTask.await()

                val downloadUrl = imageRef.downloadUrl.await()
                Log.d("StorageManager", "Subida exitosa. URL: $downloadUrl")

                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Log.e("StorageManager", "Error subiendo imagen de usuario", e)
                if (e is StorageException) {
                    Log.e("StorageManager", "Storage Error Code: ${e.errorCode}")
                    // Nota: El error 13021 es NOT_AUTHORIZED (revisar reglas de Firebase Storage)
                    // El error "The server has terminated the upload session" a veces ocurre por problemas de red
                    // o cuando el servidor rechaza la sesión resumible.
                }
                Result.failure(e)
            }
        }

    private fun checkAuth() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("StorageManager", "Advertencia: No hay un usuario autenticado. La subida podría fallar según las reglas de seguridad.")
        } else {
            Log.d("StorageManager", "Usuario autenticado para la subida: ${user.uid}")
        }
    }

    /**
     * Guarda la imagen localmente en el almacenamiento interno de la app.
     * Útil cuando Cloud Storage no está disponible (Plan Spark limitado).
     */
    suspend fun saveImageLocally(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Si la URI ya es una URL de Cloudinary (HTTPS), no necesitamos guardarla localmente como un archivo del ContentResolver
            if (uri.scheme == "http" || uri.scheme == "https") {
                Log.d("StorageManager", "La URI es remota (Cloudinary), saltando guardado local: $uri")
                return@withContext Result.success(uri.toString())
            }

            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo original"))

            // Crear un nombre único para el archivo
            val fileName = "plant_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(context.filesDir, fileName)

            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val localUri = Uri.fromFile(destinationFile).toString()
            Log.d("StorageManager", "Imagen guardada localmente en: $localUri")
            Result.success(localUri)
        } catch (e: Exception) {
            Log.e("StorageManager", "Error al guardar imagen localmente", e)
            Result.failure(e)
        }
    }
}