package com.uce.floracare.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.uce.floracare.domain.model.UserProfile

class GetUserProfileUC {
    operator fun invoke(): UserProfile? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.let {
            UserProfile(
                name = it.displayName ?: "Usuario de FloraCare",
                email = it.email ?: "",
                photoUrl = it.photoUrl?.toString()
            )
        }
    }
}
