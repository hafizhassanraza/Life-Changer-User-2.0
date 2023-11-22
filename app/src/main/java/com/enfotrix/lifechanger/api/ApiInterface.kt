package com.enfotrix.lifechanger.api

import android.app.Notification
import com.enfotrix.lifechanger.Models.Notificaion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
@Headers(
    "Content-Type:application/json",
    "Authorization:key=AAAADogOHME:APA91bFkr6fFM-F7zCDWCzhLAwrQh7k2C2wwc17U2ANoTDax-8kO8DtyLWhxxCLNPFiUpKA0LnxSlGWD1wjrQYDxSwcaRtN5-185IvhvCwiDbD-M1Ur9UHiQNdfwVOgUlUFj3KYk0Wvq"
)
    @POST("fcm/send")
    fun sendNotification(@Body notification: Notificaion): Call<Notification>

}