package com.enfotrix.life_changer_user_2_0.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiInterface = retrofit.create(ApiInterface::class.java)
}
