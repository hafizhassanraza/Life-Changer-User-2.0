package com.enfotrix.lifechanger.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.enfotrix.lifechanger.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService(){
    val channelId="AssignId"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {

val manager=getSystemService(NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)


        val notification=NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.announcement_icon)
            .setAutoCancel(false)
            .setContentIntent(null)
            .build()

        manager.notify(Random.nextInt(),notification)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager)
    {
val channel=NotificationChannel(channelId,"assignwork",NotificationManager.IMPORTANCE_HIGH)
    .apply {
        description="New work"
        enableLights(true)
    }

        manager.createNotificationChannel(channel)
    }
}