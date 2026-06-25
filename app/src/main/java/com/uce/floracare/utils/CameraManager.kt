package com.uce.floracare.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraManager (private val context: Context) {


    // Objeto responsable de tomar y guardar la foto
    private var imageCapture: ImageCapture? = null

    /**
     * Inicia la cámara y la vincula al ciclo de vida del Fragmento.
     */
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(context)

        cameraProviderFuture.addListener({
            // Obtenemos el proveedor de la cámara
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configuramos la Vista Previa (lo que el usuario ve en pantalla)
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Configuramos la captura de imagen
            imageCapture = ImageCapture.Builder().build()

            // Seleccionamos la cámara trasera por defecto
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvinculamos usos previos antes de vincular los nuevos
                cameraProvider.unbindAll()

                // Vinculamos la cámara al ciclo de vida
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraManager", "Error al vincular la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Toma la foto y la guarda en la caché del dispositivo.
     * Devuelve el Uri si es exitoso, o una Excepción si falla.
     */
    fun takePhoto(onPhotoSaved: (Uri) -> Unit, onError: (Exception) -> Unit) {
        // Asegurarnos de que imageCapture esté inicializado
        val imageCapture = imageCapture ?: return

        // Creamos un archivo temporal en la memoria caché para no saturar la galería del usuario
        val photoFile = File(
            context.externalCacheDir,
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Opciones de salida para CameraX
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Tomar la foto
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraManager", "Error al capturar foto: ${exc.message}", exc)
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // ¡Éxito! Usamos FileProvider para obtener una Uri segura
                    val savedUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    onPhotoSaved(savedUri)
                }
            }
        )
    }




}