package com.example.chillmusic.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object API {
    val apiService: ApiService
        get() = Retrofit.Builder()
            .baseUrl("https://api.musixmatch.com/ws/1.1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}