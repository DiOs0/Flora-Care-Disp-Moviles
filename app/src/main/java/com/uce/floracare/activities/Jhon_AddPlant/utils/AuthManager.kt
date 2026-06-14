package com.uce.floracare.activities.Jhon_AddPlant.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    // Obtenemos la instancia única de Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Devuelve el usuario actual logueado.
     * Retorna null si no hay ninguna sesión activa.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    /**
     * Devuelve directamente el UID del usuario.
     * ¡Esta es la función mágica que usarán Reyes y Milan!
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Retorna la instancia de FirebaseAuth por si necesitas
     * hacer operaciones específicas en Login.kt
     */
    public fun getAuthInstance(): FirebaseAuth {
        return auth
    }

}