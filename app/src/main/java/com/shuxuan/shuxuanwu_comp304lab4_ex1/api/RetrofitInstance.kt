package com.shuxuan.shuxuanwu_comp304lab4_ex1.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: DirectionsApiService by lazy {
        retrofit.create(DirectionsApiService::class.java)
    }
}