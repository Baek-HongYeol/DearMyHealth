package com.dearmyhealth.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit: Retrofit
    get() = Retrofit.Builder()
        .baseUrl("https://apis.data.go.kr/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()