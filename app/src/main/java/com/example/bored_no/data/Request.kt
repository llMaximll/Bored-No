package com.example.bored_no.data

data class Request(
    val activity: String,
    val accessibility: Float,
    val type: String,
    val participants: Int,
    val price: Float,
    val key: Int
)
