package com.uce.floracare.activities.Jhon_AddPlant.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Devuelve el usuario actual logueado.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Devuelve el UID del usuario de forma segura.
     * Retorna null si no hay sesión activa.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }

    fun getAuthInstance(): FirebaseAuth {
        return auth
    }
}
