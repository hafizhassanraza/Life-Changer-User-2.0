package com.enfotrix.life_changer_user_2_0.Models

import com.google.firebase.Timestamp

data class NotificationModel @JvmOverloads constructor (

    var id: String = "",
    var userID: String = "",
    var date: String = "",
    var notiTitle: String = "",
    var notiData: String = "",
    var read: Boolean=false,
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp


)

{
}