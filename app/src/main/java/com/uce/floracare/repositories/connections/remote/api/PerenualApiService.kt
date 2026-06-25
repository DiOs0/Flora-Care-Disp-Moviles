package com.uce.floracare.repositories.connections.remote.api

import com.uce.floracare.data.local.dto.PerenualResponse
import com.uce.floracare.repositories.connections.remote.api.RetrofitClient
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PerenualApiService {

    @GET("api/v2/species/details/{id}")
    suspend fun getPlantDetails(
        @Path("id") id: Int,
        @Query("key") apiKey: String = RetrofitClient.API_KEY
    ): PerenualResponse
}