package com.shuxuan.shuxuanwu_comp304lab4_ex1.api


import com.shuxuan.shuxuanwu_comp304lab4_ex1.data.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface DirectionsApiService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String
    ): Response<DirectionsResponse>
}

