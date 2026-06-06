package com.uce.floracare.activities.Jhon_AddPlant

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraManager (fragment: AddPlantFragment,
    private val onPhotoCaptured : (Uri?) -> Unit )

{

    // actual foto de la camara
    private var currentPhotoUri: Uri? = null

    // Esto se registra automáticamente cuando instancias e
    private val takePhotoLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            onPhotoCaptured(currentPhotoUri) // ¡Éxito! Devolvemos la URI
        } else {
            onPhotoCaptured(null) // Cancelado o falló
        }
    }

    fun openCamera(context: Context) {
        val photoFile = createImageFile(context)
        photoFile?.let {
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
            // Ya no armamos un Intent, solo le pasamos la URI al contrato
            takePhotoLauncher.launch(currentPhotoUri)
        }
    }


    private fun createImageFile(context: Context): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: Exception) {
            null
        }
    }




}