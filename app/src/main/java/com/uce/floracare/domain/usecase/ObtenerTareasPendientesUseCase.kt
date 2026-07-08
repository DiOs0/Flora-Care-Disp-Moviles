package com.uce.floracare.domain.usecase


import com.uce.floracare.repositories.TaskRepository

class ObtenerTareasPendientesUseCase(

    private val repository:TaskRepository

){

    suspend operator fun invoke()=
        repository.getTasks()

}