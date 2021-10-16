package com.example.bored_no

import com.example.bored_no.network.RetrofitService

class MainRepository constructor(private val retrofitService: RetrofitService) {

    suspend fun getRandom(
        type: String? = null,
        accessibility: Float? = null,
        participants: Int? = null,
        price: Float? = null
    ) = retrofitService.getRandom(type = type, accessibility = accessibility, participants = participants, price = price)
}