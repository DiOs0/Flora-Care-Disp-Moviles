package com.uce.floracare.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uce.floracare.domain.model.UserProfile
import com.uce.floracare.domain.usecase.GetUserProfileUC
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val getUserProfileUC: GetUserProfileUC,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val profile = getUserProfileUC()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            val userMap = mapOf(
                "uid" to user.uid,
                "name" to name,
                "photoUrl" to photoUrl,
                "email" to user.email
            )
            firestore.collection("users").document(user.uid).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
