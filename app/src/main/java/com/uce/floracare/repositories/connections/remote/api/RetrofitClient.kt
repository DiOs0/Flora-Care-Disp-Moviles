package com.uce.floracare.repositories.connections.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // PON AQUÍ TU API KEY DE PERENUAL
    const val API_KEY = "sk-bOG26a2b7fca0d1e818071"

    private const val BASE_URL = "https://perenual.com/"

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}