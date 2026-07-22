package com.uce.floracare.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.uce.floracare.domain.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firebaseAuth: FirebaseAuth =
        FirebaseAuth.getInstance(),

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    suspend fun getCurrentUserProfile():
            Result<UserProfile?> {

        return try {

            val user =
                firebaseAuth.currentUser
                    ?: return Result.failure(
                        Exception(
                            "Usuario no autenticado"
                        )
                    )

            val document =
                firestore.collection(
                    USERS_COLLECTION
                )
                    .document(
                        user.uid
                    )
                    .get()
                    .await()

            if (!document.exists()) {

                val initialProfile =
                    UserProfile(
                        name =
                            user.displayName
                                ?: "Usuario FloraCare",

                        email =
                            user.email
                                ?: "",

                        photoUrl =
                            null
                    )

                createInitialProfile(
                    initialProfile
                ).getOrThrow()

                Result.success(
                    initialProfile
                )

            } else {

                val profile =
                    UserProfile(
                        name =
                            document.getString(
                                FIELD_NAME
                            )
                                ?: "Usuario FloraCare",

                        email =
                            document.getString(
                                FIELD_EMAIL
                            )
                                ?: user.email
                                ?: "",

                        photoUrl =
                            document.getString(
                                FIELD_PHOTO_URL
                            )
                    )

                Result.success(
                    profile
                )
            }

        } catch (e: Exception) {

            Result.failure(
                e
            )
        }
    }

    suspend fun updateUserProfile(
        name: String,
        photoUrl: String?
    ): Result<Unit> {

        return try {

            val user =
                firebaseAuth.currentUser
                    ?: return Result.failure(
                        Exception(
                            "Usuario no autenticado"
                        )
                    )

            val userMap =
                mutableMapOf<String, Any?>(
                    FIELD_UID to user.uid,
                    FIELD_NAME to name,
                    FIELD_EMAIL to user.email
                )

            /*
             * Solo actualiza la foto cuando existe
             * una URL válida.
             *
             * Así no borra la foto anterior cuando
             * el usuario cambia únicamente el nombre.
             */
            if (!photoUrl.isNullOrBlank()) {

                userMap[FIELD_PHOTO_URL] =
                    photoUrl
            }

            firestore.collection(
                USERS_COLLECTION
            )
                .document(
                    user.uid
                )
                .set(
                    userMap,
                    SetOptions.merge()
                )
                .await()

            Result.success(
                Unit
            )

        } catch (e: Exception) {

            Result.failure(
                e
            )
        }
    }

    private suspend fun createInitialProfile(
        profile: UserProfile
    ): Result<Unit> {

        return try {

            val user =
                firebaseAuth.currentUser
                    ?: return Result.failure(
                        Exception(
                            "Usuario no autenticado"
                        )
                    )

            val userMap =
                mapOf(
                    FIELD_UID to user.uid,
                    FIELD_NAME to profile.name,
                    FIELD_EMAIL to profile.email,
                    FIELD_PHOTO_URL to profile.photoUrl
                )

            firestore.collection(
                USERS_COLLECTION
            )
                .document(
                    user.uid
                )
                .set(
                    userMap,
                    SetOptions.merge()
                )
                .await()

            Result.success(
                Unit
            )

        } catch (e: Exception) {

            Result.failure(
                e
            )
        }
    }

    fun logout() {

        firebaseAuth.signOut()
    }

    companion object {

        private const val USERS_COLLECTION =
            "users"

        private const val FIELD_UID =
            "uid"

        private const val FIELD_NAME =
            "name"

        private const val FIELD_EMAIL =
            "email"

        private const val FIELD_PHOTO_URL =
            "photoUrl"
    }
}