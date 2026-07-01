package com.uce.floracare.repositories.connections.remote.cloudinary

import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object CloudinaryService {

    // Credenciales (Extraídas de tu configuración previa)
    private const val CLOUD_NAME = "deqhd3bmp"
    private const val API_KEY = "188973848385489"
    private const val API_SECRET = "bmPFYmcccVKbOhp5g0U6LyHn8aE"

    fun subirImagenFirmada(
        archivoImagen: File,
        onResultado: (Success: Boolean, urlOrError: String) -> Unit
    ) {
        val rutaLocal = archivoImagen.absolutePath

        MediaManager.get().upload(rutaLocal)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("CloudinaryService", "Subida iniciada...")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progreso = (bytes.toDouble() / totalBytes * 100).toInt()
                    Log.d("CloudinaryService", "Progreso: $progreso%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val urlSegura = resultData["secure_url"] as? String ?: ""
                    Log.d("CloudinaryService", "¡Éxito! URL: $urlSegura")
                    onResultado(true, urlSegura)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("CloudinaryService", "Error: ${error?.description}")
                    onResultado(false, error?.description ?: "Error generico")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("CloudinaryService", "Subida reprogramada: ${error?.description}")
                }
            })
            .dispatch()
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     * Ejemplo: .../v12345/nombre_imagen.jpg -> nombre_imagen
     */
    fun extraerPublicId(url: String): String? {
        return try {
            // Maneja URLs con versiones (v1234567) y carpetas
            val parts = url.split("/")
            val lastPart = parts.last()
            val publicIdWithExtension = lastPart.substringBeforeLast(".")
            
            // Si hay carpetas antes del nombre del archivo, se deben incluir.
            // Para este proyecto, asumimos que están en la raíz o extraemos el último segmento.
            publicIdWithExtension
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ELIMINACIÓN REAL: Usa la API REST de Cloudinary mediante una solicitud POST firmada.
     */
    suspend fun eliminarImagen(publicId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signature = generarFirma(publicId, timestamp)

            val url = URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true

            val postData = "public_id=$publicId&timestamp=$timestamp&api_key=$API_KEY&signature=$signature"
            
            conn.outputStream.use { it.write(postData.toByteArray()) }

            val responseCode = conn.responseCode
            val responseMessage = conn.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("CloudinaryService", "Imagen eliminada físicamente: $publicId")
                true
            } else {
                Log.e("CloudinaryService", "Error API Cloudinary ($responseCode): $responseMessage")
                false
            }
        } catch (e: Exception) {
            Log.e("CloudinaryService", "Error crítico al eliminar de Cloudinary", e)
            false
        }
    }

    /**
     * Genera la firma SHA-1 requerida por Cloudinary para acciones administrativas (destroy).
     * El orden de los parámetros DEBE ser alfabético.
     */
    private fun generarFirma(publicId: String, timestamp: String): String {
        val stringToSign = "public_id=$publicId&timestamp=$timestamp$API_SECRET"
        return sha1(stringToSign)
    }

    private fun sha1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
