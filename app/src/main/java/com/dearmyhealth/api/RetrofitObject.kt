package com.dearmyhealth.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitObject {
    private const val baseUrl = "https://apis.data.go.kr/"
    const val API_KEY =
        "SBuPOuYlnFu3bWXmWOYjCrFUEVE4xTbOMtIo0vH+/fgUEpkJakKQbu4t4/t7+ZVhErj5S8Z+bHRd/cwPPNd51w=="

    private val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    val apiService: Apiservice by lazy {
        retrofit.create(Apiservice::class.java)
    }

    suspend fun getDurItems(itemSeq: String): Response<DurItem> {
        return apiService.getDurItemInfo(
            ItemSeq = itemSeq,
            ServiceKey = API_KEY,
            PageNo = 1,
            NumOfRows = 10,
            Type = "json",
            ItemName = "itemName",
            EntpName = "entpName",
            StartChangeDate = "20200101",
            EndChangeDate = "20201231",
            Bizrno = "bizrno",
            TypeName = "typeName",
            IngrCode = "ingrCode"
        )
    }
}