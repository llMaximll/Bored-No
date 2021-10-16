package com.example.bored_no.network

import com.example.bored_no.data.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("/api/activity/")
    suspend fun getRandom(
        @Query("type") type: String? = null,
        @Query("accessibility") accessibility: Float? = null,
        @Query("participants") participants: Int? = null,
        @Query("price") price: Float? = null
    ): Response<Request>

    companion object {
        var retrofitService: RetrofitService? = null
        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://www.boredapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }

            return retrofitService!!
        }
    }
}