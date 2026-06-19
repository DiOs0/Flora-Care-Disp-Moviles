package com.uce.floracare.activities.Milan_Ajustes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uce.floracare.domain.model.UserProfile
import com.uce.floracare.domain.usecase.GetUserProfileUC

class AjustesViewModel(
    private val getUserProfileUC: GetUserProfileUC
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfile

    fun loadUserProfile() {
        _userProfile.value = getUserProfileUC()
    }
}
